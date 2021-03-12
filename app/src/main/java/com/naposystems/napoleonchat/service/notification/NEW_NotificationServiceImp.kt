package com.naposystems.napoleonchat.service.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.RemoteMessage
import com.naposystems.napoleonchat.BuildConfig
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.app.NapoleonApplication
import com.naposystems.napoleonchat.crypto.message.CryptoMessage
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.service.syncManager.SyncManager
import com.naposystems.napoleonchat.source.remote.dto.newMessageEvent.NewMessageEventMessageRes
import com.naposystems.napoleonchat.source.remote.dto.validateMessageEvent.ValidateMessage
import com.naposystems.napoleonchat.ui.mainActivity.MainActivity
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Data
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.utility.Utils
import com.pusher.client.connection.ConnectionState
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class NEW_NotificationServiceImp
@Inject constructor(
    private val context: Context,
    private val socketNotificationService: SocketNotificationService,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val syncManager: SyncManager,
    private val cryptoMessage: CryptoMessage
) : NotificationService {

    var queueDataNotifications: MutableList<Map<String, String>> = mutableListOf()
    var queueNotifications: MutableList<RemoteMessage.Notification?> = mutableListOf()

    private var notificationCount: Int = 0

    companion object {
        const val NOTIFICATION_RINGING = 950707
        const val NOTIFICATION_UPLOADING = 20102020
        const val NOTIFICATION = 162511

        const val SUMMARY_ID = 12345678
        const val GROUP_MESSAGE = "GROUP_MESSAGE"

        val mediaPlayer: MediaPlayer = MediaPlayer()
    }

    private val moshi: Moshi by lazy {
        Moshi.Builder().build()
    }

    private val disposable: CompositeDisposable by lazy {
        CompositeDisposable()
    }

    private val app: NapoleonApplication by lazy {
        context as NapoleonApplication
    }

    override fun createInformativeNotification(
        data: Map<String, String>,
        notification: RemoteMessage.Notification?
    ) {

        Timber.d("**Paso 1: Notificacion Recibida $data")

        notificationCount = if (data.containsKey(Constants.NotificationKeys.BADGE))
            data.getValue(Constants.NotificationKeys.BADGE).toInt()
        else
            0


        Timber.d("**Paso 1.1: Notificacion Count $notificationCount")

        if (data.containsKey(Constants.NotificationKeys.MESSAGE_ID))
            if (!validateExistMessageId(data.getValue(Constants.NotificationKeys.MESSAGE_ID))) {
                Timber.d("**Paso 2: Registro en la cola $data")
                queueDataNotifications.add(data)
                queueNotifications.add(notification)
            }

        Timber.d("**Paso 3.1: Estados Status Socket: ${socketNotificationService.getStatusSocket()} Status Channel: ${socketNotificationService.getStatusGlobalChannel()} ")

        if (socketNotificationService.getStatusSocket() != ConnectionState.CONNECTED &&
            socketNotificationService.getStatusGlobalChannel() != Constants.SocketChannelStatus.SOCKECT_CHANNEL_STATUS_CONNECTED.status
        ) {
            Timber.d("**Paso 3.2: Solicitud de conexion. Status Socket: ${socketNotificationService.getStatusSocket()} Status Channel: ${socketNotificationService.getStatusGlobalChannel()} ")
            socketNotificationService.connectSocket()
            listenConnectChannel()
        } else {
            Timber.d("**Paso 3.3: Solicitud a proceso de cola desde el principal")
            processQueueNotifications()
        }
    }

    private fun listenConnectChannel() {
        val disposableNotification = RxBus
            .listen(RxEvent.CreateNotification::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                Timber.d("**Paso 3.3: Solicitud a proceso de cola desde la conexion del canal")
                processQueueNotifications()
            }
        disposable.add(disposableNotification)
    }

    private fun processQueueNotifications() {

        Timber.d("**Paso 4: Proceso de cola ${queueDataNotifications.size}")

        while (queueDataNotifications.size > 0) {

            Timber.d("**Paso 5: Cola superior a cero")

            var itemDataNotification = queueDataNotifications.first()

            var itemNotification = queueNotifications.first()

            Timber.d("**Paso 6: Proceso del item $itemDataNotification")

            queueDataNotifications.removeFirst()

            queueNotifications.removeFirst()

            syncManager.insertMessage(itemDataNotification.getValue(Constants.NotificationKeys.MESSAGE))

            emitClientConversation(itemDataNotification.getValue(Constants.NotificationKeys.MESSAGE))

            syncManager.notifyMessageReceived(itemDataNotification.getValue(Constants.NotificationKeys.MESSAGE_ID))

            if (!itemDataNotification.getValue(Constants.NotificationKeys.SILENCE).toBoolean()) {

                Timber.d("**Paso 10: No Silenciado")

                showNotification(itemDataNotification, itemNotification)

            }

            //AQUI VOY A MOSTRAR LA NOTIFICACION

            Timber.d("NUEVISIMO NUEVA DATACOLA $queueDataNotifications")
        }
    }

    private fun validateExistMessageId(messageId: String): Boolean {
        var exist: Boolean = false
        for (item in queueDataNotifications) {
            if (item.get("message_id").equals(messageId)) {
                exist = true
            }
        }
        return exist
    }


    private fun emitClientConversation(messageString: String) {

        Timber.d("**Paso 8: Proceso de Emision del item $messageString")

        GlobalScope.launch() {
            val newMessageEventMessageResData: String = if (BuildConfig.ENCRYPT_API) {
                cryptoMessage.decryptMessageBody(messageString)
            } else {
                messageString
            }

            Timber.d("Paso 8.1: Desencriptar mensaje $messageString")
            try {
                val jsonAdapter: JsonAdapter<NewMessageEventMessageRes> =
                    moshi.adapter(NewMessageEventMessageRes::class.java)

                jsonAdapter.fromJson(newMessageEventMessageResData)
                    ?.let { newMessageEventMessageRes ->

                        val messages = arrayListOf(
                            ValidateMessage(
                                id = newMessageEventMessageRes.id,
                                user = newMessageEventMessageRes.userAddressee,
                                status = Constants.MessageEventType.UNREAD.status
                            )
                        )

                        Timber.d("**Paso 8.2: Emitir Recibido $messages")

                        socketNotificationService.emitClientConversation(messages)

                    }
            } catch (e: java.lang.Exception) {
                Timber.e("${e.localizedMessage} $newMessageEventMessageResData")
            }

        }
    }

    private fun showNotification(
        itemDataNotification: Map<String, String>,
        itemNotification: RemoteMessage.Notification?
    ) {

        Timber.d("**Paso 10.1 : Proceso del Item mostrar notificacion itemDataNotification: $itemDataNotification itemNotification $itemNotification")

        var notificationType = 0

        val title = itemNotification?.title

        val body = itemNotification?.body

        val pair =
            createPendingIntent(
                itemDataNotification,
                notificationType
            )

        val pendingIntent = pair.first

        notificationType = pair.second

        val contactIdNotification =
            if (itemDataNotification.containsKey(Constants.NotificationKeys.CONTACT))
                itemDataNotification.getValue(Constants.NotificationKeys.CONTACT).toInt()
            else
                null

        val iconBitmap = BitmapFactory.decodeResource(
            context.resources, R.drawable.ic_notification_icon
        )

        if (Data.contactId != contactIdNotification) {
            Utils.vibratePhone(context, Constants.Vibrate.DEFAULT.type, 100)
            Handler(Looper.getMainLooper()).postDelayed({
                Utils.vibratePhone(context, Constants.Vibrate.DEFAULT.type, 100)
            }, 200)
        }

        val channelId = getChannelType(notificationType, contactIdNotification)

        val builder = NotificationCompat.Builder(
            context,
            channelId
        )
            .setLargeIcon(iconBitmap)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentTitle(title)
            .setContentIntent(pendingIntent)
            .setContentText(body)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setNumber(notificationCount)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            .setAutoCancel(true)

        if (itemDataNotification.containsKey(Constants.NotificationKeys.TITLE)) {
            builder.setContentTitle(itemDataNotification.getValue(Constants.NotificationKeys.TITLE))
        }

        if (itemDataNotification.containsKey(Constants.NotificationKeys.BODY)) {
            builder.setContentText(itemDataNotification.getValue(Constants.NotificationKeys.BODY))
        }

        if (!app.isAppVisible()) {

            Timber.d("**Paso 10.3 : Muestra Notificacion")

            with(NotificationManagerCompat.from(context)) {
                notify(123456, builder.build())
                disposable.clear()
            }

        }

    }

    private fun getChannelType(
        notificationType: Int,
        contactId: Int? = null
    ): String {
        return when (notificationType) {
            Constants.NotificationType.ENCRYPTED_MESSAGE.type -> {
                val contact = contactId?.let {
                    syncManager.getContactById(it)
                }
                contact?.let {
                    if (it.stateNotification) {
                        context.getString(
                            R.string.notification_custom_channel_id,
                            it.getNickName(),
                            it.notificationId
                        )
                    } else {
                        context.getString(
                            R.string.notification_message_channel_id,
                            syncManager.getNotificationMessageChannelId()
                        )
                    }
                } ?: kotlin.run {
                    context.getString(
                        R.string.notification_message_channel_id,
                        syncManager.getNotificationMessageChannelId()
                    )
                }
            }
            else -> {
                context.getString(R.string.default_notification_channel_id)
            }
        }
    }

    private fun createPendingIntent(
        data: Map<String, String>,
        notificationType: Int
    ): Pair<PendingIntent, Int> {

        Timber.d("**Paso 10.2 : Crear Pending Intent data: $data notificationType $notificationType")

        var notificationTypeAux = notificationType

        val notificationIntent = Intent(context, MainActivity::class.java)

        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)

        with(data) {
            if (this.isNotEmpty()) {
                val typeNotificationKey =
                    Constants.NotificationKeys.TYPE_NOTIFICATION
                val contactKey =
                    Constants.NotificationKeys.CONTACT
                val messageKey =
                    Constants.NotificationKeys.MESSAGE_ID

                val attackKey = Constants.NotificationKeys.ATTACK

                if (this.containsKey(typeNotificationKey)) {
                    notificationTypeAux = this.getValue(typeNotificationKey).toInt()
                    notificationIntent.putExtra(typeNotificationKey, notificationTypeAux.toString())
                }

                if (this.containsKey(contactKey)) {
                    notificationIntent.putExtra(contactKey, this.getValue(contactKey).toString())
                }

                if (this.containsKey(messageKey)) {
                    notificationIntent.putExtra(messageKey, this.getValue(messageKey).toString())
                }

                if (this.containsKey(attackKey)) {
                    notificationIntent.putExtra(attackKey, this.getValue(attackKey).toString())
                }
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_ONE_SHOT
        )

        return Pair(pendingIntent, notificationTypeAux)
    }

}
