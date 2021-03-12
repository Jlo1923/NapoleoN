package com.naposystems.napoleonchat.service.notification

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.core.app.NotificationCompat.*
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.app.NapoleonApplication
import com.naposystems.napoleonchat.crypto.message.CryptoMessage
import com.naposystems.napoleonchat.service.handlerChannel.HandlerChannel
import com.naposystems.napoleonchat.service.socket.NEWSocketService
import com.naposystems.napoleonchat.service.syncManager.SyncManager
import com.naposystems.napoleonchat.service.webRTCCall.WebRTCCallService
import com.naposystems.napoleonchat.ui.conversationCall.ConversationCallActivity
import com.naposystems.napoleonchat.ui.mainActivity.MainActivity
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Utils
import com.squareup.moshi.Moshi
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

class OLD_NotificationService
@Inject constructor(
    private val context: Context,
    private val syncManager: SyncManager,
    private val handlerChannelService: HandlerChannel.Service,
    private val cryptoMessage: CryptoMessage,
    private val newSocketService: NEWSocketService,
) {

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

    private val app: NapoleonApplication by lazy {
        context as NapoleonApplication
    }

    private val disposable: CompositeDisposable by lazy {
        CompositeDisposable()
    }

    private var notificationCount: Int = 0

    init {
//        (applicationContext as DaggerApplication).androidInjector().inject(this)

        handlerChannelService.initializeChannels()

    }

    //region Handler Messages
//    private fun listenEncryptMessage(
//        data: Map<String, String>,
//        builder: Builder,
//        context: Context
//    ) {
//        val disposableNotification = RxBus
//            .listen(RxEvent.CreateNotification::class.java)
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe {
//                Timber.d("RXBUS ESCUCHADOR")
//                Timber.d("DATAAAAA: $data")
//                createEncryptMessage(data, builder, context)
//            }
//
//        disposable.add(disposableNotification)
//
//    }

//    private fun createEncryptMessage(
//        data: Map<String, String>,
//        builder: Builder,
//        context: Context
//    ) {
//
//        Timber.d("Paso 2: se va a crear la notifiacion data: $data")
//
//        val contact = Constants.NotificationKeys.CONTACT
//
//        syncManager.getContactSilenced(
//            data.getValue(contact).toInt(),
//            silenced = { silenced ->
//                if (silenced != null && silenced == true) {
//                    Timber.d("--- Esta silenciada la mka esa xd")
//                } else {
//                    if (Data.contactId != data.getValue(contact).toInt()) {
//                        Utils.vibratePhone(context, Constants.Vibrate.DEFAULT.type, 100)
//                        Handler(Looper.getMainLooper()).postDelayed({
//                            Utils.vibratePhone(context, Constants.Vibrate.DEFAULT.type, 100)
//                        }, 200)
//                    }
//
//                    val titleKey =
//                        Constants.NotificationKeys.TITLE
//                    val bodyKey =
//                        Constants.NotificationKeys.BODY
//                    val messageId =
//                        Constants.NotificationKeys.MESSAGE_ID
//                    val message = Constants.NotificationKeys.MESSAGE
//
//                    if (data.containsKey(titleKey)) {
//                        builder.setContentTitle(data.getValue(titleKey))
//                    }
//
//                    if (data.containsKey(bodyKey)) {
//                        builder.setContentText(data.getValue(bodyKey))
//                    }
//
//                    Timber.d("*NotificationTest: isVisible ${app.isAppVisible()}")
//
//                    if (data.containsKey(message) && !app.isAppVisible()) {
//
//                        Timber.d("Paso 3: insercion mensaje $data")
//
//                        syncManager.insertMessage(data.getValue(message))
//
//                        validateMessageEvent(data.getValue(message))
//                    }
//
//                    if (data.containsKey(messageId) && !app.isAppVisible()) {
//
//                        Timber.d("Paso 9: Notifica recibido $data")
//
//                        syncManager.notifyMessageReceived(data.getValue(messageId))
//                    }
//
//                    if (!app.isAppVisible()) {
//                        Timber.d("Paso 10: Muestra notificacion")
//                        with(NotificationManagerCompat.from(context)) {
//                            notify(123456, builder.build())
//                            disposable.clear()
//                        }
//                    }
//                }
//            })
//    }

//    private fun validateMessageEvent(messageString: String) {
//
//        Timber.d(
//            "Paso 4: voy a insertar el mensaje $messageString"
//        )
//
//        GlobalScope.launch(Dispatchers.IO) {
//            var newMessageEventMessageResData: String = messageString
//
//            if (BuildConfig.ENCRYPT_API) {
//                try {
//                    newMessageEventMessageResData = cryptoMessage.decryptMessageBody(messageString)
//                } catch (e: java.lang.Exception) {
//                    Timber.e(messageString)
//                }
//            }
//
//            Timber.d("Paso 5: Desencriptar mensaje $messageString")
//
//            try {
//
//                val jsonAdapter: JsonAdapter<NewMessageEventMessageRes> =
//                    moshi.adapter(NewMessageEventMessageRes::class.java)
//
//                jsonAdapter.fromJson(newMessageEventMessageResData)
//                    ?.let { newMessageEventMessageRes ->
//
//                        try {
//                            val messages = arrayListOf(
//                                ValidateMessage(
//                                    id = newMessageEventMessageRes.id,
//                                    user = newMessageEventMessageRes.userAddressee,
//                                    status = Constants.MessageEventType.UNREAD.status
//                                )
//                            )
//
//                            socketService.emitClientConversation(messages)
//
//                        } catch (e: Exception) {
//                            Timber.e(e)
//                        }
//                    }
//            } catch (e: java.lang.Exception) {
//                Timber.e("${e.localizedMessage} $newMessageEventMessageResData")
//            }
//
//
//        }
//
//    }
    //endregion

    //region Upload
    fun createUploadNotification(
        context: Context
    ): Notification {
        val notificationBuilder = Builder(
            context,
            context.getString(R.string.alerts_channel_id)
        )
            .setSmallIcon(R.drawable.ic_file_upload_black)
            .setContentTitle(context.getString(R.string.text_sending_file))
            .setContentText(context.getString(R.string.text_sending_file))
            .setProgress(0, 0, true)
            .setOngoing(true)

        return notificationBuilder.build()
    }

    fun updateUploadProgress(max: Int, progress: Int) {
        val notificationBuilder = Builder(
            context,
            context.getString(R.string.alerts_channel_id)
        )
            .setSmallIcon(R.drawable.ic_file_upload_black)
            .setContentTitle(context.getString(R.string.text_sending_file))
            .setContentText(context.getString(R.string.text_sending_file))
            .setProgress(max, progress, false)
            .setOngoing(true)

        val notification = notificationBuilder.build()

        val mNotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.notify(NOTIFICATION_UPLOADING, notification)
    }

    //endregion

    //region General
    private fun playRingTone(context: Context) {
        try {
            Utils.getAudioManager(context).isSpeakerphoneOn = true
            mediaPlayer.apply {
                Timber.d("*Test: Play Ring")
                setAudioStreamType(AudioManager.STREAM_NOTIFICATION)
//                reset()
                if (isPlaying) {
//                    stop()
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

    fun stopMediaPlayer() {
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

    private fun createPendingIntent(
        context: Context,
        data: Map<String, String>,
        notificationType: Int
    ): Pair<PendingIntent, Int> {
        var notificationType1 = notificationType
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

                val attackKey = Constants.NotificationKeys.ATTACKER_ID

                if (this.containsKey(typeNotificationKey)) {
                    notificationType1 = this.getValue(typeNotificationKey).toInt()
                    notificationIntent.putExtra(typeNotificationKey, notificationType1.toString())
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
        return Pair(pendingIntent, notificationType1)
    }

//
//    fun createInformativeNotification(
//        context: Context,
//        data: Map<String, String>,
//        notification: RemoteMessage.Notification?
//    ) {
//        Timber.d("*Notification: Created")
//        var notificationType = 0
//        val sharedPreferencesManager =
//            SharedPreferencesManager(context)
//        val title = notification?.title
//        val body = notification?.body
//
//
//        val iconBitmap = BitmapFactory.decodeResource(
//            context.resources, R.drawable.ic_notification_icon
//        )
//
//        notificationCount = if (data.containsKey("badge")) data.getValue("badge").toInt() else 0
//
//        Timber.d("*Notification: $notificationCount")
//
//        val pair =
//            createPendingIntent(
//                context,
//                data,
//                notificationType
//            )
//        val pendingIntent = pair.first
//
//        notificationType = pair.second
//
//        Timber.d("*TestNotification: Data -> $data")
//        val channelId = if (data.containsKey("contact")) {
//            handlerChannelService.getChannelType(notificationType, data.getValue("contact").toInt())
//        } else {
//            handlerChannelService.getChannelType(notificationType)
//        }
//
//        Timber.d(channelId)
//
//        val builder = Builder(
//            context,
//            channelId
//        )
//            .setLargeIcon(iconBitmap)
//            .setSmallIcon(R.drawable.ic_notification_icon)
//            .setContentTitle(title)
//            .setContentIntent(pendingIntent)
//            .setContentText(body)
//            .setDefaults(DEFAULT_ALL)
//            .setPriority(PRIORITY_MAX)
//            .setNumber(0)
//            .setVisibility(VISIBILITY_PUBLIC)
//            .setBadgeIconType(BADGE_ICON_SMALL)
//            .setAutoCancel(true)
//
//        Timber.d("DATAAAAA: $data")
//
//        if (notificationType == Constants.NotificationType.ENCRYPTED_MESSAGE.type) {
//            builder.setNumber(notificationCount)
//            listenEncryptMessage(data, builder, context)
//        }
//
//        handleNotificationType(
//            notificationType,
//            data,
//            builder,
//            context,
//            sharedPreferencesManager
//        )
//    }

//    private fun handleNotificationType(
//        notificationType: Int,
//        data: Map<String, String>,
//        builder: Builder,
//        context: Context,
//        sharedPreferencesManager: SharedPreferencesManager
//    ) {
//
//        when (notificationType) {
//
//            Constants.NotificationType.ENCRYPTED_MESSAGE.type -> {
//
//                if (!app.isAppVisible()) {
//
//                    Timber.d(" Paso 1: handleNotificationType: $notificationType, $data")
//
//                    socketService.connectSocket(Constants.LocationConnectSocket.FROM_NOTIFICATION.location)
//                }
//            }
//
//            Constants.NotificationType.NEW_FRIENDSHIP_REQUEST.type -> {
//                RxBus.publish(RxEvent.NewFriendshipRequest())
//            }
//
//            Constants.NotificationType.FRIEND_REQUEST_ACCEPTED.type -> {
//                RxBus.publish(RxEvent.FriendshipRequestAccepted())
//            }
//
//            Constants.NotificationType.VERIFICATION_CODE.type,
//            Constants.NotificationType.SUBSCRIPTION.type -> {
//                with(NotificationManagerCompat.from(context)) {
//                    notify(Random().nextInt(), builder.build())
//                }
//            }
//
//            Constants.NotificationType.ACCOUNT_ATTACK.type -> {
//                val attackerId = data.getValue("attacker_id").toString()
//
//                sharedPreferencesManager.putInt(
//                    Constants.SharedPreferences.PREF_EXISTING_ATTACK,
//                    Constants.ExistingAttack.EXISTING.type
//                )
//                sharedPreferencesManager.putString(
//                    Constants.SharedPreferences.PREF_ATTACKER_ID, attackerId
//                )
//
//                with(NotificationManagerCompat.from(context)) {
//                    notify(Random().nextInt(), builder.build())
//                }
//
//                RxBus.publish(RxEvent.AccountAttack())
//            }
//
//            Constants.NotificationType.INCOMING_CALL.type -> {
//                Timber.d("Incoming call, ${syncManager.getIsOnCallPref()}")
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
//            }
//            Constants.NotificationType.CANCEL_CALL.type -> {
//                Timber.d("CANCEL_CALL")
//                Data.isOnCall = false
//                val notificationManager =
//                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//                notificationManager.cancelAll()
//            }
//            Constants.NotificationType.USER_AVAILABLE_FOR_CALL.type -> {
//                Timber.d("USER_AVAILABLE_FOR_CALL")
//                if (!app.isAppVisible()) {
//                    var channel = ""
//
//                    if (data.containsKey(Constants.CallKeys.CHANNEL)) {
//                        channel = "presence-${data[Constants.CallKeys.CHANNEL]}"
//                    }
//                    socketService.connectToSocketReadyForCall(channel)
//                }
//            }
//        }
//    }

    private fun getServiceNotificationAction(
        context: Context,
        action: String,
        iconResId: Int,
        titleResId: Int,
        channel: String,
        contactId: Int,
        isVideoCall: Boolean
    ): Action? {

        val intent = Intent(context, WebRTCCallService::class.java)
        intent.action = action
        val bundle = Bundle()

        bundle.putString(
            Constants.CallKeys.CHANNEL,
            channel
        )

        bundle.putBoolean(
            Constants.CallKeys.IS_VIDEO_CALL,
            isVideoCall
        )

        bundle.putInt(
            Constants.CallKeys.CONTACT_ID,
            contactId
        )
        intent.putExtras(bundle)
        val pendingIntent =
            PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        return Action(
            iconResId,
            context.getString(titleResId),
            pendingIntent
        )
    }
    //endregion

    //region Llamada
    fun startWebRTCCallService(
        channel: String,
        isVideoCall: Boolean,
        contactId: Int,
        isIncomingCall: Boolean,
        context: Context
    ) {
        val service = Intent(context, WebRTCCallService::class.java)

        val bundle = Bundle()

        bundle.putString(
            Constants.CallKeys.CHANNEL,
            channel
        )

        bundle.putBoolean(
            Constants.CallKeys.IS_VIDEO_CALL,
            isVideoCall
        )

        bundle.putInt(
            Constants.CallKeys.CONTACT_ID,
            contactId
        )

        bundle.putBoolean(
            Constants.CallKeys.IS_INCOMING_CALL,
            isIncomingCall
        )

        service.putExtras(bundle)

        context.startService(service)
    }

    fun createCallNotification(
        channel: String,
        contactId: Int,
        isVideoCall: Boolean,
        context: Context
    ): Notification {

        Timber.d("createCallNotification: $isVideoCall")

        val fullScreenIntent =
            Intent(context, ConversationCallActivity::class.java).apply {
                putExtras(Bundle().apply {
                    putInt(ConversationCallActivity.CONTACT_ID, contactId)
                    putString(ConversationCallActivity.CHANNEL, channel)
                    putBoolean(ConversationCallActivity.IS_VIDEO_CALL, isVideoCall)
                    putBoolean(ConversationCallActivity.IS_INCOMING_CALL, true)
                    putBoolean(ConversationCallActivity.IS_FROM_CLOSED_APP, true)
                })
            }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context, 0,
            fullScreenIntent, 0
        )

        val contact = syncManager.getContact(contactId)

        val notificationTitle = if (!isVideoCall) {
            context.getString(R.string.text_incoming_secure_call)
        } else {
            context.getString(R.string.text_incoming_secure_video_call)
        }

        val notificationBuilder = Builder(
            context,
            context.getString(if (app.isAppVisible()) R.string.alerts_channel_id else R.string.calls_channel_id)
        )
            .setSmallIcon(R.drawable.ic_call_black_24)
            .setGroup(this.context.getString(R.string.calls_group_key))
            .setContentTitle("@${contact?.getNickName()}")
            .setContentText(notificationTitle)
            .setOngoing(true)
            .setCategory(CATEGORY_CALL)
            .addAction(
                getServiceNotificationAction(
                    context,
                    WebRTCCallService.ACTION_DENY_CALL,
                    R.drawable.ic_close_black_24,
                    R.string.text_reject,
                    channel, contactId, isVideoCall
                )
            )
            .addAction(
                getServiceNotificationAction(
                    context,
                    WebRTCCallService.ACTION_ANSWER_CALL,
                    R.drawable.ic_call_black_24,
                    R.string.text_answer_call,
                    channel, contactId, isVideoCall
                )
            )

        if (callActivityRestricted(context) && !app.isAppVisible()) {
            // Use a full-screen intent only for the highest-priority alerts where you
            // have an associated activity that you would like to launch after the user
            // interacts with the notification. Also, if your app targets Android 10
            // or higher, you need to request the USE_FULL_SCREEN_INTENT permission in
            // order for the platform to invoke this notification.
            notificationBuilder.setFullScreenIntent(fullScreenPendingIntent, true)
            notificationBuilder.priority = PRIORITY_HIGH
            notificationBuilder.setCategory(CATEGORY_CALL)
            playRingTone(context)
        }

        return notificationBuilder.build()
    }

    private fun callActivityRestricted(context: Context): Boolean {
        return Build.VERSION.SDK_INT >= 29 && !(context as NapoleonApplication).isAppVisible()
    }

    fun createCallingNotification(
        channel: String,
        contactId: Int,
        isVideoCall: Boolean,
        context: Context
    ): Notification {
        val notificationTitle = if (!isVideoCall) {
            context.getString(R.string.text_secure_outgoing_call)
        } else {
            context.getString(R.string.text_secure_outgoing_video_call)
        }

        val notificationBuilder = Builder(
            context,
            context.getString(R.string.alerts_channel_id)
        )
            .setGroup(this.context.getString(R.string.calls_group_key))
            .setSmallIcon(R.drawable.ic_call_black_24)
            .setContentTitle(notificationTitle)
            .setContentText(context.getString(R.string.text_calling_call_title))
            .setCategory(CATEGORY_CALL)
            .setOngoing(true)
            .addAction(
                getServiceNotificationAction(
                    context,
                    WebRTCCallService.ACTION_HANG_UP,
                    R.drawable.ic_close_black_24,
                    R.string.text_hang_up_call,
                    channel, contactId, isVideoCall
                )
            )

        return notificationBuilder.build()
    }

    fun updateCallInProgress(channel: String, contactId: Int, isVideoCall: Boolean) {
        val notificationBuilder = Builder(
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
                    context,
                    WebRTCCallService.ACTION_HANG_UP,
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
    //endregion

    private fun createSummaryNotification(context: Context): Notification {
        // Create an Intent for the activity you want to start
        val resultIntent = Intent(context, MainActivity::class.java)
        // Create the TaskStackBuilder
        val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(context).run {
            // Add the intent, which inflates the back stack
            addNextIntentWithParentStack(resultIntent)
            // Get the PendingIntent containing the entire back stack
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        return Builder(context, context.getString(R.string.default_notification_channel_id))
//            .setContentTitle("Messages")
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setStyle(
                InboxStyle()
//                    .setBigContentTitle("Messages")
                    .setSummaryText(context.getString(R.string.text_count_messages))
            )
            .setPriority(PRIORITY_LOW)
            .setContentIntent(resultPendingIntent)
            .setGroupAlertBehavior(GROUP_ALERT_CHILDREN)
            .setGroup(GROUP_MESSAGE)
            .setGroupSummary(true)
            .build()
    }

    /*fun getNotificationId(context: Context, type: Int): Int {
        return if (callActivityRestricted(context) && type == Constants.NotificationType.INCOMING_CALL.type) {
            NOTIFICATION_RINGING
        } else {
            NOTIFICATION
        }
    }*/

    /*private fun createGroupChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Timber.d("*NotificationTest: createGroupChannel")

            val channelId = "Notification Group"
            val name = "Notification Group"
            val descriptionText = "Notification Group Description"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            channel.setShowBadge(true)
            channel.lockscreenVisibility = PRIORITY_MAX
            channel.group = context.getString(R.string.category_channel_chat)

            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }*/

}
