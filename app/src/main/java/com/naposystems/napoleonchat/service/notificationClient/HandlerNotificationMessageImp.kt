package com.naposystems.napoleonchat.service.notificationClient

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import com.google.firebase.messaging.RemoteMessage
import com.naposystems.napoleonchat.BuildConfig
import com.naposystems.napoleonchat.app.NapoleonApplication
import com.naposystems.napoleonchat.crypto.message.CryptoMessage
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.service.socketClient.SocketClient
import com.naposystems.napoleonchat.service.syncManager.SyncManager
import com.naposystems.napoleonchat.source.remote.dto.conversation.message.MESSAGE
import com.naposystems.napoleonchat.source.remote.dto.newMessageEvent.NewMessageEventMessageRes
import com.naposystems.napoleonchat.source.remote.dto.validateMessageEvent.ValidateMessage
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Constants.NotificationKeys.SILENCE
import com.naposystems.napoleonchat.utility.Utils
import com.pusher.client.connection.ConnectionState
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class HandlerNotificationMessageImp
@Inject constructor(
    private val context: Context,
    private val socketClient: SocketClient,
    private val syncManager: SyncManager,
    private val cryptoMessage: CryptoMessage,
    private val handlerNotification: HandlerNotification,
) : HandlerNotificationMessage {

    companion object {
        const val SUMMARY_ID = 12345678
        val mediaPlayer: MediaPlayer = MediaPlayer()
    }

    var queueDataNotifications: MutableList<Map<String, String>> = mutableListOf()

    var queueNotifications: MutableList<RemoteMessage.Notification?> = mutableListOf()

    private val moshi: Moshi by lazy {
        Moshi.Builder().build()
    }

    private val disposable: CompositeDisposable by lazy {
        CompositeDisposable()
    }

    override fun handlerMessage(
        dataFromNotification: Map<String, String>,
        notification: RemoteMessage.Notification?
    ) {
        Timber.d("**Paso 1: Notificacion Recibida $dataFromNotification")

        if (dataFromNotification.containsKey(Constants.NotificationKeys.MESSAGE_ID)) {
            if (!validateExistMessageId(dataFromNotification.getValue(Constants.NotificationKeys.MESSAGE_ID))) {
                Timber.d("**Paso 2: Registro en la cola $dataFromNotification")
                queueDataNotifications.add(dataFromNotification)
                queueNotifications.add(notification)
            }
        }

        if (socketClient.getStatusSocket() == ConnectionState.CONNECTED &&
            socketClient.getStatusGlobalChannel() == Constants.SocketChannelStatus.SOCKECT_CHANNEL_STATUS_CONNECTED.status
        )
            processQueueMessagesNotifications()
        else {
            socketClient.connectSocket()
            listenConnectChannel()
        }
    }

    private fun validateExistMessageId(messageId: String): Boolean {
        var exist = false
        loop@ for (item in queueDataNotifications) {
            if (item.getValue(Constants.NotificationKeys.MESSAGE_ID) == messageId) {
                exist = true
                break@loop
            }
        }
        return exist
    }

    private fun listenConnectChannel() {
        val disposableNotification = RxBus
            .listen(RxEvent.CreateNotification::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                Timber.d("**Paso 3.3: Solicitud a proceso de cola desde la conexion del canal")
                processQueueMessagesNotifications()
            }
        disposable.add(disposableNotification)
    }

    private fun processQueueMessagesNotifications() {

        Timber.d("**Paso 4: Proceso de cola ${queueDataNotifications.size}")

        while (queueDataNotifications.size > 0) {

            Timber.d("**Paso 5: Cola superior a cero")

            val itemDataNotification = queueDataNotifications.first()

            val itemNotification = queueNotifications.first()

            Timber.d("**Paso 6: Proceso del item $itemDataNotification")

            queueDataNotifications.removeFirst()

            queueNotifications.removeFirst()

            syncManager.insertMessage(itemDataNotification.getValue(Constants.NotificationKeys.MESSAGE))

            emitClientConversation(itemDataNotification.getValue(Constants.NotificationKeys.MESSAGE))

            syncManager.notifyMessageReceived(
                ValidateMessage(
                    itemDataNotification.getValue(Constants.NotificationKeys.MESSAGE_ID),
                    status = 0, user = 0
                )
            )

            if (itemDataNotification.getValue(SILENCE).toBoolean().not()) {
                Timber.d("**Paso 10: No Silenciado")
                processNotification(itemDataNotification, itemNotification)
            }

            Timber.d("NUEVISIMO NUEVA DATACOLA $queueDataNotifications")
        }
    }

    private fun processNotification(
        itemDataNotification: Map<String, String>,
        itemNotification: RemoteMessage.Notification?
    ) {

        Timber.d("**Paso 10.1 : Proceso del Item mostrar notificacion itemDataNotification: $itemDataNotification itemNotification $itemNotification")

        val contactIdNotification =
            if (itemDataNotification.containsKey(Constants.NotificationKeys.CONTACT))
                itemDataNotification.getValue(Constants.NotificationKeys.CONTACT).toInt()
            else
                null

        if (NapoleonApplication.currentConversationContactId != contactIdNotification) {
            Utils.vibratePhone(context, Constants.Vibrate.DEFAULT.type, 100)
            Handler(Looper.getMainLooper()).postDelayed({
                Utils.vibratePhone(context, Constants.Vibrate.DEFAULT.type, 100)
            }, 200)
        }

        if (NapoleonApplication.isVisible.not()) {

            Timber.d("**Paso 10.3 : Muestra Notificacion")

            handlerNotification.showNotification(
                itemDataNotification,
                itemNotification,
                SUMMARY_ID
            )
            disposable.clear()
        }
    }

    private fun emitClientConversation(messageString: String) {

        Timber.d("**Paso 8: Proceso de Emision del item $messageString")

        GlobalScope.launch() {
            val newMessageEventMessageResData: String =
                if (BuildConfig.ENCRYPT_API)
                    cryptoMessage.decryptMessageBody(messageString)
                else
                    messageString

            Timber.d("Paso 8.1: Desencriptar mensaje $messageString")
            try {
                val jsonAdapter: JsonAdapter<NewMessageEventMessageRes> =
                    moshi.adapter(NewMessageEventMessageRes::class.java)

                jsonAdapter.fromJson(newMessageEventMessageResData)
                    ?.let { newMessageEventMessageRes ->

                        val messages = arrayListOf(
                            ValidateMessage(
                                id = newMessageEventMessageRes.id,
                                user = newMessageEventMessageRes.userAddressee.toLong(),
                                status = Constants.MessageEventType.UNREAD.status
                            )
                        )

                        Timber.d("**Paso 8.2: Emitir Recibido $messages")

                        socketClient.emitClientConversation(messages)

                    }
            } catch (e: java.lang.Exception) {
                Timber.e("${e.localizedMessage} $newMessageEventMessageResData")
            }
        }
    }
}
