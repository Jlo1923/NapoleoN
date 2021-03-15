package com.naposystems.napoleonchat.service.notificationMessage

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
import com.naposystems.napoleonchat.service.handlerNotificationChannel.HandlerNotificationChannel
import com.naposystems.napoleonchat.service.socketMessage.SocketMessageService
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
import java.util.*
import javax.inject.Inject

class NotificationMessagesServiceImp
@Inject constructor(
    private val context: Context,
    private val socketMessageService: SocketMessageService,
    private val handlerNotificationChannelService: HandlerNotificationChannel.Service,
    private val syncManager: SyncManager,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val cryptoMessage: CryptoMessage
) : NotificationMessagesService {

    var queueDataNotifications: MutableList<Map<String, String>> = mutableListOf()
    var queueNotifications: MutableList<RemoteMessage.Notification?> = mutableListOf()

//    private var notificationCount: Int = 0

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

    init {
        handlerNotificationChannelService.initializeChannels()
    }

    override fun createInformativeNotification(
        dataFromNotification: Map<String, String>,
        notification: RemoteMessage.Notification?
    ) {

        when (dataFromNotification.getValue(Constants.NotificationKeys.TYPE_NOTIFICATION)
            ?.toInt()) {

            Constants.NotificationType.VERIFICATION_CODE.type,
            Constants.NotificationType.SUBSCRIPTION.type -> {
                showNotification(createNotificationBuilder(dataFromNotification, notification))
            }

            Constants.NotificationType.ENCRYPTED_MESSAGE.type -> {

                if (!app.isAppVisible())
                    handlerMessage(dataFromNotification, notification)

            }

            Constants.NotificationType.NEW_FRIENDSHIP_REQUEST.type -> {
                RxBus.publish(RxEvent.NewFriendshipRequest())
            }

            Constants.NotificationType.FRIEND_REQUEST_ACCEPTED.type -> {
                RxBus.publish(RxEvent.FriendshipRequestAccepted())
            }

            Constants.NotificationType.ACCOUNT_ATTACK.type -> {

                val attackerId =
                    dataFromNotification.getValue(Constants.NotificationKeys.ATTACKER_ID).toString()

                sharedPreferencesManager.putInt(
                    Constants.SharedPreferences.PREF_EXISTING_ATTACK,
                    Constants.ExistingAttack.EXISTING.type
                )

                sharedPreferencesManager.putString(
                    Constants.SharedPreferences.PREF_ATTACKER_ID,
                    attackerId
                )

                showNotification(createNotificationBuilder(dataFromNotification, notification))

                RxBus.publish(RxEvent.AccountAttack())
            }

            Constants.NotificationType.INCOMING_CALL.type -> {
                Timber.d("Incoming call, ${syncManager.getIsOnCallPref()}")
//                if (!syncManager.getIsOnCallPref()) {
//                    socketService.connectSocket(Constants.LocationConnectSocket.FROM_APP.location)
//                    Timber.d("Incoming call 2")
//                    var channel = ""
//                    var contactId = 0
//                    var isVideoCall = false
//
//                    if (data.containsKey(Constants.CallKeys.CHANNEL)) {
//                        channel = "presence-${data[Constants.CallKeys.CHANNEL]}"
//                    }
//
//                    if (data.containsKey(Constants.CallKeys.IS_VIDEO_CALL)) {
//                        isVideoCall = data[Constants.CallKeys.IS_VIDEO_CALL] == "true"
//                        Timber.d("Call: ${data[Constants.CallKeys.IS_VIDEO_CALL] == "true"}")
//                    }
//
//                    if (data.containsKey(Constants.CallKeys.CONTACT_ID)) {
//                        contactId = data[Constants.CallKeys.CONTACT_ID]?.toInt() ?: 0
//                    }
//
//                    if (channel != "presence-" && contactId != 0) {
//                        startWebRTCCallService(channel, isVideoCall, contactId, true, context)
//                    }
//                }
            }
            Constants.NotificationType.CANCEL_CALL.type -> {
                Timber.d("CANCEL_CALL")
//                Data.isOnCall = false
//                val notificationManager =
//                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//                notificationManager.cancelAll()
            }
            Constants.NotificationType.USER_AVAILABLE_FOR_CALL.type -> {
                Timber.d("USER_AVAILABLE_FOR_CALL")
//                if (!app.isAppVisible()) {
//                    var channel = ""
//
//                    if (data.containsKey(Constants.CallKeys.CHANNEL)) {
//                        channel = "presence-${data[Constants.CallKeys.CHANNEL]}"
//                    }
//                    socketService.connectToSocketReadyForCall(channel)
//                }
            }
        }
    }

    private fun handlerMessage(
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

        Timber.d("**Paso 3.1: Estados Status Socket: ${socketMessageService.getStatusSocket()} Status Channel: ${socketMessageService.getStatusGlobalChannel()} ")

        if (socketMessageService.getStatusSocket() == ConnectionState.CONNECTED &&
            socketMessageService.getStatusGlobalChannel() == Constants.SocketChannelStatus.SOCKECT_CHANNEL_STATUS_CONNECTED.status
        ) {
            Timber.d("**Paso 3.2: Solicitud a proceso de cola desde el principal")
            processQueueMessagesNotifications()
        } else {
            Timber.d("**Paso 3.3: Solicitud de conexion. Status Socket: ${socketMessageService.getStatusSocket()} Status Channel: ${socketMessageService.getStatusGlobalChannel()} ")
            socketMessageService.connectSocket()
            listenConnectChannel()
        }
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

            var itemDataNotification = queueDataNotifications.first()

            var itemNotification = queueNotifications.first()

            Timber.d("**Paso 6: Proceso del item $itemDataNotification")

            queueDataNotifications.removeFirst()

            queueNotifications.removeFirst()

            syncManager.insertMessage(itemDataNotification.getValue(Constants.NotificationKeys.MESSAGE))

            emitClientConversation(itemDataNotification.getValue(Constants.NotificationKeys.MESSAGE))

            syncManager.notifyMessageReceived(itemDataNotification.getValue(Constants.NotificationKeys.MESSAGE_ID))

            if (!itemDataNotification.getValue(Constants.NotificationKeys.SILENCE)
                    .toBoolean()
            ) {

                Timber.d("**Paso 10: No Silenciado")

                processNotification(itemDataNotification, itemNotification)

            }

            Timber.d("NUEVISIMO NUEVA DATACOLA $queueDataNotifications")
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

                        socketMessageService.emitClientConversation(messages)

                    }
            } catch (e: java.lang.Exception) {
                Timber.e("${e.localizedMessage} $newMessageEventMessageResData")
            }

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

        if (Data.contactId != contactIdNotification) {
            Utils.vibratePhone(context, Constants.Vibrate.DEFAULT.type, 100)
            Handler(Looper.getMainLooper()).postDelayed({
                Utils.vibratePhone(context, Constants.Vibrate.DEFAULT.type, 100)
            }, 200)
        }

        if (!app.isAppVisible()) {

            Timber.d("**Paso 10.3 : Muestra Notificacion")

            showNotification(
                createNotificationBuilder(itemDataNotification, itemNotification),
                SUMMARY_ID
            )

            disposable.clear()

        }

    }

    private fun createPendingIntent(
        dataFromNotification: Map<String, String>
    ): PendingIntent? {

        Timber.d("**Paso 10.2 : Crear Pending Intent data: $dataFromNotification")

        val notificationIntent = Intent(context, MainActivity::class.java)

        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)

        with(dataFromNotification) {

            if (this.isNotEmpty()) {

                if (this.containsKey(Constants.NotificationKeys.CONTACT)) {
                    notificationIntent.putExtra(
                        Constants.NotificationKeys.CONTACT,
                        this.getValue(Constants.NotificationKeys.CONTACT).toString()
                    )
                }

                if (this.containsKey(Constants.NotificationKeys.MESSAGE_ID)) {
                    notificationIntent.putExtra(
                        Constants.NotificationKeys.MESSAGE_ID,
                        this.getValue(Constants.NotificationKeys.MESSAGE_ID).toString()
                    )
                }

                if (this.containsKey(Constants.NotificationKeys.ATTACKER_ID)) {
                    notificationIntent.putExtra(
                        Constants.NotificationKeys.ATTACKER_ID,
                        this.getValue(Constants.NotificationKeys.ATTACKER_ID).toString()
                    )
                }

            }

        }

        return PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_ONE_SHOT
        )
    }

    private fun createNotificationBuilder(
        dataFromNotification: Map<String, String>,
        notification: RemoteMessage.Notification?
    ): NotificationCompat.Builder {

        val pendingIntent = createPendingIntent(
            dataFromNotification
        )

        val iconBitmap = BitmapFactory.decodeResource(
            context.resources, R.drawable.ic_notification_icon
        )

        val channelType =
            if (dataFromNotification.containsKey(Constants.NotificationKeys.CONTACT)) {
                handlerNotificationChannelService.getChannelType(
                    dataFromNotification.getValue(Constants.NotificationKeys.TYPE_NOTIFICATION)
                        .toInt(),
                    dataFromNotification.getValue(Constants.NotificationKeys.CONTACT).toInt()
                )
            } else {
                handlerNotificationChannelService.getChannelType(
                    dataFromNotification.getValue(Constants.NotificationKeys.TYPE_NOTIFICATION)
                        .toInt()
                )
            }

        val notificationCount =
            if (dataFromNotification.containsKey(Constants.NotificationKeys.BADGE))
                dataFromNotification.getValue(Constants.NotificationKeys.BADGE).toInt()
            else
                0

        return NotificationCompat.Builder(
            context,
            channelType
        )
            .setLargeIcon(iconBitmap)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentTitle(dataFromNotification.getValue(Constants.NotificationKeys.TITLE))
            .setContentText(dataFromNotification.getValue(Constants.NotificationKeys.BODY))
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setNumber(notificationCount)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            .setAutoCancel(true)

    }

    private fun showNotification(
        builder: NotificationCompat.Builder,
        notificationId: Int = Random().nextInt()
    ) {
        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }

}
