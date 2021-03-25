package com.naposystems.napoleonchat.service.notificationMessage

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
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
import com.naposystems.napoleonchat.webRTC.service.WebRTCService
import com.naposystems.napoleonchat.source.remote.dto.newMessageEvent.NewMessageEventMessageRes
import com.naposystems.napoleonchat.source.remote.dto.validateMessageEvent.ValidateMessage
import com.naposystems.napoleonchat.ui.conversationCall.ConversationCallActivity
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
    private val napoleonApplication: NapoleonApplication,
    private val socketMessageService: SocketMessageService,
    private val handlerNotificationChannelService: HandlerNotificationChannel.Service,
    private val syncManager: SyncManager,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val cryptoMessage: CryptoMessage
) : NotificationMessagesService {

    var queueDataNotifications: MutableList<Map<String, String>> = mutableListOf()

    var queueNotifications: MutableList<RemoteMessage.Notification?> = mutableListOf()

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

    init {
        handlerNotificationChannelService.initializeChannels()
    }

    override fun createNotification(
        dataFromNotification: Map<String, String>,
        notification: RemoteMessage.Notification?
    ) {

        when (dataFromNotification.getValue(Constants.NotificationKeys.TYPE_NOTIFICATION).toInt()) {
            Constants.NotificationType.VERIFICATION_CODE.type,
            Constants.NotificationType.SUBSCRIPTION.type -> {
                showNotification(
                    createNotificationMessageBuilder(
                        dataFromNotification,
                        notification
                    )
                )
            }

            Constants.NotificationType.ENCRYPTED_MESSAGE.type -> {
                if (!napoleonApplication.visible)
                    handlerMessage(dataFromNotification, notification)
            }

            Constants.NotificationType.NEW_FRIENDSHIP_REQUEST.type -> {
                RxBus.publish(RxEvent.NewFriendshipRequest())
            }

            Constants.NotificationType.FRIEND_REQUEST_ACCEPTED.type -> {
                RxBus.publish(RxEvent.FriendshipRequestAccepted())
            }

            Constants.NotificationType.ACCOUNT_ATTACK.type -> {

                sharedPreferencesManager.putInt(
                    Constants.SharedPreferences.PREF_EXISTING_ATTACK,
                    Constants.ExistingAttack.EXISTING.type
                )

                sharedPreferencesManager.putString(
                    Constants.SharedPreferences.PREF_ATTACKER_ID,
                    dataFromNotification.getValue(Constants.NotificationKeys.ATTACKER_ID).toString()
                )

                showNotification(
                    createNotificationMessageBuilder(
                        dataFromNotification,
                        notification
                    )
                )

                RxBus.publish(RxEvent.AccountAttack())
            }

            Constants.NotificationType.INCOMING_CALL.type -> {

                //TODO: Revisar aqui el estado de la vista y de la llamada
                if (!napoleonApplication.visible) {
//                if (!syncManager.getIsOnCallPref() && !Data.isShowingCallActivity) {
                    socketMessageService.connectSocket()
                    Timber.d("Incoming call, 2")
                    var channel = ""
                    var contactId = 0
                    var isVideoCall = false
                    var offer = ""

                    if (dataFromNotification.containsKey(Constants.CallKeys.CHANNEL_NAME))
                        channel =
                            "presence-${dataFromNotification[Constants.CallKeys.CHANNEL_NAME]}"

                    if (dataFromNotification.containsKey(Constants.CallKeys.IS_VIDEO_CALL))
                        isVideoCall =
                            dataFromNotification[Constants.CallKeys.IS_VIDEO_CALL] == "true"

                    if (dataFromNotification.containsKey(Constants.CallKeys.CONTACT_ID))
                        contactId =
                            dataFromNotification[Constants.CallKeys.CONTACT_ID]?.toInt() ?: 0

                    if (dataFromNotification.containsKey(Constants.CallKeys.OFFER))
                        offer = dataFromNotification[Constants.CallKeys.OFFER].toString()

                    socketMessageService.subscribeToCallChannelFromBackground(channel)

                    if (channel != "presence-" && contactId != 0 && offer.isNotEmpty()) {
                        startWebRTCCallService(
                            channel,
                            isVideoCall,
                            contactId,
                            true,
                            offer
                        )
                    }
                }
            }

            Constants.NotificationType.CANCEL_CALL.type -> {
                Timber.d("CANCEL_CALL")
                Data.isOnCall = false
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancelAll()
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

        if (socketMessageService.getStatusSocket() == ConnectionState.CONNECTED &&
            socketMessageService.getStatusGlobalChannel() == Constants.SocketChannelStatus.SOCKECT_CHANNEL_STATUS_CONNECTED.status
        )
            processQueueMessagesNotifications()
        else {
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

        if (!napoleonApplication.visible) {

            Timber.d("**Paso 10.3 : Muestra Notificacion")

            showNotification(
                createNotificationMessageBuilder(itemDataNotification, itemNotification),
                SUMMARY_ID
            )

            disposable.clear()

        }

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

    private fun createNotificationMessageBuilder(
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


        val builder = NotificationCompat.Builder(
            context,
            channelType
        )
            .setContentTitle(notification?.title)
            .setContentText(notification?.body)
            .setNumber(0)

        if (dataFromNotification.containsKey(Constants.NotificationKeys.TITLE))
            builder.setContentTitle(
                dataFromNotification.getValue(Constants.NotificationKeys.TITLE)
            )

        if (dataFromNotification.containsKey(Constants.NotificationKeys.BODY))
            builder.setContentText(
                dataFromNotification.getValue(Constants.NotificationKeys.BODY)
            )

        if (dataFromNotification.containsKey(Constants.NotificationKeys.BADGE))
            builder.setNumber(
                dataFromNotification.getValue(Constants.NotificationKeys.BADGE).toInt()
            )

        return builder
            .setLargeIcon(iconBitmap)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_MAX)
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
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////

    override fun stopMediaPlayer() {
        try {
            Timber.d("*Test: Stop Ring")
            if (mediaPlayer.isPlaying) {
                mediaPlayer.reset()
//                mediaPlayer.release()
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun startWebRTCCallService(
        channel: String,
        isVideoCall: Boolean,
        contactId: Int,
        isIncomingCall: Boolean,
        offer: String
    ) {
        val service = Intent(context, WebRTCService::class.java).apply {
            putExtras(Bundle().apply {
                putString(Constants.CallKeys.CHANNEL_NAME, channel)
                putBoolean(Constants.CallKeys.IS_VIDEO_CALL, isVideoCall)
                putInt(Constants.CallKeys.CONTACT_ID, contactId)
                putBoolean(Constants.CallKeys.IS_INCOMING_CALL, isIncomingCall)
                putString(Constants.CallKeys.OFFER, offer)
            })
        }

        context.startService(service)
    }

    override fun updateCallInProgress(channel: String, contactId: Int, isVideoCall: Boolean) {
        val notificationBuilder = NotificationCompat.Builder(
            context,
            context.getString(R.string.alerts_channel_id)
        )
            .setGroup(context.getString(R.string.calls_group_key))
            .setSmallIcon(R.drawable.ic_call_black_24)
            .setUsesChronometer(true)
            .setContentTitle(context.getString(R.string.text_call_in_progress))
            .setOngoing(true)
            .addAction(
                getServiceNotificationAction(
                    WebRTCService.ACTION_HANG_UP,
                    R.drawable.ic_close_black_24,
                    R.string.text_hang_up_call,
                    channel, contactId, isVideoCall
                )
            )

        val notification = notificationBuilder.build()

        val notificationId = NOTIFICATION_RINGING

        Timber.d("notificationId: $notificationId")

        val mNotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.notify(notificationId, notification)
    }

    override fun createNotificationCallBuilder(
        channel: String,
        contactId: Int,
        isVideoCall: Boolean,
        typeCall: Int,
        offer: String,
    ): Notification {

        val contact = syncManager.getContact(contactId)

        val notificationBuilder = NotificationCompat.Builder(
            context,
            context.getString(if (napoleonApplication.visible) R.string.alerts_channel_id else R.string.calls_channel_id)
        ).apply {
            setSmallIcon(R.drawable.ic_call_black_24)
            setGroup(context.getString(R.string.calls_group_key))
            setContentTitle("@${contact?.getNickName()}")
            setContentText(getTexNotification(typeCall, isVideoCall))
            setCategory(NotificationCompat.CATEGORY_CALL)
            setOngoing(true)
            if (typeCall == Constants.TypeCall.IS_INCOMING_CALL.type) {
                addAction(
                    getServiceNotificationAction(
                        WebRTCService.ACTION_DENY_CALL,
                        R.drawable.ic_close_black_24,
                        R.string.text_reject,
                        channel, contactId, isVideoCall
                    )
                )
                addAction(
                    getServiceNotificationAction(
                        WebRTCService.ACTION_ANSWER_CALL,
                        R.drawable.ic_call_black_24,
                        R.string.text_answer_call,
                        channel, contactId, isVideoCall, offer
                    )
                )
            } else {
                addAction(
                    getServiceNotificationAction(
                        WebRTCService.ACTION_HANG_UP,
                        R.drawable.ic_close_black_24,
                        R.string.text_hang_up_call,
                        channel, contactId, isVideoCall
                    )
                )
            }

        }

        if (typeCall == Constants.TypeCall.IS_INCOMING_CALL.type) {

            val fullScreenIntent =
                Intent(context, ConversationCallActivity::class.java).apply {
                    putExtras(Bundle().apply {
                        putInt(ConversationCallActivity.CONTACT_ID, contactId)
                        putString(ConversationCallActivity.CHANNEL, channel)
                        putBoolean(ConversationCallActivity.IS_VIDEO_CALL, isVideoCall)
                        putInt(
                            ConversationCallActivity.TYPE_CALL,
                            Constants.TypeCall.IS_INCOMING_CALL.type
                        )
                        putString(ConversationCallActivity.OFFER, offer)
                        putBoolean(ConversationCallActivity.IS_FROM_CLOSED_APP, true)
                    })
                }

            val fullScreenPendingIntent = PendingIntent.getActivity(
                context, 0,
                fullScreenIntent, 0
            )

            if (Build.VERSION.SDK_INT >= 29 && !napoleonApplication.visible) {
                notificationBuilder.apply {
                    setFullScreenIntent(fullScreenPendingIntent, true)
                    priority = NotificationCompat.PRIORITY_HIGH
                }
                playRingTone()
            }
        }

        return notificationBuilder.build()

    }

    private fun getTexNotification(typeCall: Int, isVideoCall: Boolean): String {

        return if (typeCall == Constants.TypeCall.IS_INCOMING_CALL.type) {
            if (!isVideoCall)
                context.getString(R.string.text_incoming_secure_call)
            else
                context.getString(R.string.text_incoming_secure_video_call)
        } else {
            if (!isVideoCall)
                context.getString(R.string.text_secure_outgoing_call)
            else
                context.getString(R.string.text_secure_outgoing_video_call)
        }
    }

    private fun getServiceNotificationAction(
        action: String,
        iconResId: Int,
        titleResId: Int,
        channel: String,
        contactId: Int,
        isVideoCall: Boolean,
        offer: String = ""
    ): NotificationCompat.Action {

        val intent = Intent(context, WebRTCService::class.java).apply {
            this.action = action
            putExtras(Bundle().apply {
                putString(Constants.CallKeys.CHANNEL_NAME, channel)
                putBoolean(Constants.CallKeys.IS_VIDEO_CALL, isVideoCall)
                putInt(Constants.CallKeys.CONTACT_ID, contactId)
                putString(Constants.CallKeys.OFFER, offer)
            })
        }

        val pendingIntent =
            PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        return NotificationCompat.Action(
            iconResId,
            context.getString(titleResId),
            pendingIntent
        )
    }

    private fun playRingTone() {
        try {
            Utils.getAudioManager(context).isSpeakerphoneOn = true
            mediaPlayer.apply {
                setAudioAttributes(
                    AudioAttributes
                        .Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                if (isPlaying) {
                    reset()
                }
                setDataSource(
                    context,
                    Settings.System.DEFAULT_RINGTONE_URI
                )
                this.isLooping = isLooping
                prepare()
                start()
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

}
