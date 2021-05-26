package com.naposystems.napoleonchat.service.notificationClient

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import com.google.firebase.messaging.RemoteMessage
import com.naposystems.napoleonchat.BuildConfig
import com.naposystems.napoleonchat.app.NapoleonApplication
import com.naposystems.napoleonchat.crypto.message.CryptoMessage
import com.naposystems.napoleonchat.model.toMessagesReqDTO
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.service.socketClient.GetMessagesSocketListener
import com.naposystems.napoleonchat.service.socketClient.SocketClient
import com.naposystems.napoleonchat.service.syncManager.SyncManager
import com.naposystems.napoleonchat.source.remote.dto.messagesReceived.MessagesReqDTO
import com.naposystems.napoleonchat.source.remote.dto.newMessageEvent.NewMessageEventMessageRes
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Constants.NotificationKeys.MESSAGE_ID
import com.naposystems.napoleonchat.utility.Constants.StatusMustBe.RECEIVED
import com.naposystems.napoleonchat.utility.Utils
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
) : HandlerNotificationMessage, GetMessagesSocketListener {

    companion object {
        const val SUMMARY_ID = 12345678
        val mediaPlayer: MediaPlayer = MediaPlayer()
    }

    var queueDataNotifications: MutableList<Map<String, String>> = mutableListOf()
    var queueNotifications: MutableList<RemoteMessage.Notification?> = mutableListOf()

    private val moshi: Moshi by lazy { Moshi.Builder().build() }
    private val disposable: CompositeDisposable by lazy { CompositeDisposable() }

    override fun handlerMessage(
        dataFromNotification: Map<String, String>,
        notification: RemoteMessage.Notification?
    ) {
        Timber.d("**Paso 1: Notificacion Recibida $dataFromNotification")

//        syncManager.setGetMessagesSocketListener(this)

        if (dataFromNotification.containsKey(MESSAGE_ID)) {
            if (!validateExistMessageId(dataFromNotification.getValue(MESSAGE_ID))) {
                Timber.d("**Paso 2: Registro en la cola $dataFromNotification")
                queueDataNotifications.add(dataFromNotification)
                queueNotifications.add(notification)
            }
        }

        if (socketClient.isConnected()) {
            processQueueMessagesNotifications()
        } else {
            Timber.d("LLAMADA PASO 3: HANDLER MESSAGE")
            GlobalScope.launch {
                socketClient.connectSocket()
            }
            listenConnectChannel()
        }
    }

    private fun validateExistMessageId(messageId: String): Boolean {
        var exist = false
        loop@ for (item in queueDataNotifications) {
            if (item.getValue(MESSAGE_ID) == messageId) {
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

            if (itemDataNotification.containsKey(Constants.NotificationKeys.MESSAGE)) {
                handlerTextMessage(itemDataNotification)
            } else {
                syncManager.getMyMessages(null)
            }

            if (!itemDataNotification.getValue(Constants.NotificationKeys.SILENCE).toBoolean()) {
                Timber.d("**Paso 10: No Silenciado")
                processNotification(itemDataNotification, itemNotification)
            }
            Timber.d("NUEVISIMO NUEVA DATACOLA $queueDataNotifications")
        }
    }

    private fun handlerTextMessage(itemDataNotification: Map<String, String>) {
        val messageString: String = if (BuildConfig.ENCRYPT_API) {
            cryptoMessage.decryptMessageBody(itemDataNotification.getValue(Constants.NotificationKeys.MESSAGE))
        } else {
            itemDataNotification.getValue(Constants.NotificationKeys.MESSAGE)
        }

        syncManager.insertMessage(messageString)

        val jsonAdapterMessage: JsonAdapter<NewMessageEventMessageRes> =
            moshi.adapter(NewMessageEventMessageRes::class.java)

        jsonAdapterMessage.fromJson(messageString)
            ?.let { messageModel ->
                val listMessagesToReceived = listOf(messageModel).toMessagesReqDTO(RECEIVED)
                syncManager.notifyMessageReceived(listMessagesToReceived)
                socketClient.emitClientConversation(listMessagesToReceived)
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

    override fun emitSocketClientConversation(listMessagesReceived: MessagesReqDTO) {
        socketClient.emitClientConversation(listMessagesReceived)
    }

}
