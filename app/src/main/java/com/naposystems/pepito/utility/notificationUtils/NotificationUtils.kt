package com.naposystems.pepito.utility.notificationUtils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.core.app.NotificationCompat.*
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.RemoteMessage
import com.naposystems.pepito.R
import com.naposystems.pepito.app.NapoleonApplication
import com.naposystems.pepito.reactive.RxBus
import com.naposystems.pepito.reactive.RxEvent
import com.naposystems.pepito.repository.notificationUtils.NotificationUtilsRepository
import com.naposystems.pepito.service.webRTCCall.WebRTCCallService
import com.naposystems.pepito.ui.conversationCall.ConversationCallActivity
import com.naposystems.pepito.ui.mainActivity.MainActivity
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.utility.Utils.Companion.setupNotificationSound
import com.naposystems.pepito.webService.socket.IContractSocketService
import dagger.android.support.DaggerApplication
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class NotificationUtils @Inject constructor(
    applicationContext: Context
) {

    companion object {
        const val NOTIFICATION_RINGING = 950707
        const val NOTIFICATION = 162511

        fun cancelWebRTCCallNotification(context: Context) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(NOTIFICATION_RINGING)
        }
    }

    @Inject
    lateinit var repository: NotificationUtilsRepository

    @Inject
    lateinit var socketService: IContractSocketService.SocketService

    init {
        (applicationContext as DaggerApplication).androidInjector().inject(this)
        createNotificationChannel(applicationContext)
        createCallNotificationChannel(applicationContext)
    }

    fun createInformativeNotification(
        context: Context,
        data: Map<String, String>,
        notification: RemoteMessage.Notification?
    ) {
        var notificationType = 0
        val sharedPreferencesManager =
            SharedPreferencesManager(context)
        val title = notification?.title
        val body = notification?.body
        val channelId = context.getString(R.string.default_notification_channel_id)
        val iconBitmap = BitmapFactory.decodeResource(
            context.resources, R.drawable.ic_notification_icon
        )

        val pair =
            createPendingIntent(
                context,
                data,
                notificationType
            )
        val pendingIntent = pair.first
        notificationType = pair.second

        val builder = Builder(
            context,
            channelId
        )
            .setLargeIcon(iconBitmap)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentTitle(title)
            .setContentIntent(pendingIntent)
            .setContentText(body)
            .setDefaults(DEFAULT_ALL)
            .setPriority(PRIORITY_MAX)
            .setVisibility(VISIBILITY_PUBLIC)
            .setAutoCancel(true)

        handleNotificationType(
            notificationType,
            data,
            builder,
            context,
            sharedPreferencesManager
        )
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

                if (this.containsKey(typeNotificationKey)) {
                    notificationType1 = this.getValue(typeNotificationKey).toInt()
                    notificationIntent.putExtra(typeNotificationKey, notificationType1.toString())
                }

                if (this.containsKey(contactKey)) {
                    notificationIntent.putExtra(contactKey, this.getValue(contactKey).toString())
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

    private fun handleNotificationType(
        notificationType: Int,
        data: Map<String, String>,
        builder: Builder,
        context: Context,
        sharedPreferencesManager: SharedPreferencesManager
    ) {
        var app: NapoleonApplication? = null
        if (context is NapoleonApplication) {
            app = context
            Timber.d("IsAppVisible: ${app.isAppVisible()}")
        }
        when (notificationType) {

            Constants.NotificationType.ENCRYPTED_MESSAGE.type -> {
                /*{message_id=ec45fe6f-0b7d-4255-9549-35ef9cedf2e6,
                body=Has recibido un mensaje cifrado,
                sound=default,
                title=Mensaje Cifrado,
                contact=194097,
                type_notification=1,
                silence=false}*/

                val contact = Constants.NotificationKeys.CONTACT
                repository.getContactSilenced(
                    data.getValue(contact).toInt(),
                    silenced = { silenced ->
                        if (silenced != null && silenced == true) {
                            Timber.d("--- Esta silenciada la mka esa xd")
                        } else {
                            setupNotificationSound(context, R.raw.tone_receive_message)

                            val titleKey =
                                Constants.NotificationKeys.TITLE
                            val bodyKey =
                                Constants.NotificationKeys.BODY
                            val messageId =
                                Constants.NotificationKeys.MESSAGE_ID

                            if (data.containsKey(titleKey)) {
                                builder.setContentTitle(data.getValue(titleKey))
                            }

                            if (data.containsKey(bodyKey)) {
                                builder.setContentText(data.getValue(bodyKey))
                            }

                            if (data.containsKey(messageId) && app != null && !app.isAppVisible()) {
                                repository.notifyMessageReceived(data.getValue(messageId))
                            }

                            if (!app!!.isAppVisible()) {
                                with(NotificationManagerCompat.from(context)) {
                                    notify(Random().nextInt(), builder.build())
                                }
                            }
                        }
                    })
            }

            Constants.NotificationType.NEW_FRIENDSHIP_REQUEST.type -> {
                RxBus.publish(RxEvent.NewFriendshipRequest())
            }

            Constants.NotificationType.FRIEND_REQUEST_ACCEPTED.type -> {
                RxBus.publish(RxEvent.FriendshipRequestAccepted())
            }

            Constants.NotificationType.VERIFICATION_CODE.type, Constants.NotificationType.SUBSCRIPTION.type -> {
                with(NotificationManagerCompat.from(context)) {
                    notify(Random().nextInt(), builder.build())
                }
            }

            Constants.NotificationType.ACCOUNT_ATTACK.type -> {
                val attackerId = data.getValue("attacker_id").toString()

                sharedPreferencesManager.putInt(
                    Constants.SharedPreferences.PREF_EXISTING_ATTACK,
                    Constants.ExistingAttack.EXISTING.type
                )
                sharedPreferencesManager.putString(
                    Constants.SharedPreferences.PREF_ATTACKER_ID, attackerId
                )

                with(NotificationManagerCompat.from(context)) {
                    notify(Random().nextInt(), builder.build())
                }

                RxBus.publish(RxEvent.AccountAttack())
            }

            Constants.NotificationType.INCOMING_CALL.type -> {
                Timber.d("Incoming call, ${repository.getIsOnCallPref()}")
                if (app != null && !app.isAppVisible() && !repository.getIsOnCallPref()) {
                    socketService.initSocket()
                    Timber.d("Incoming call 2")
                    var channel = ""
                    var contactId = 0
                    var isVideoCall = false

                    if (data.containsKey(Constants.CallKeys.CHANNEL)) {
                        channel = "private-${data[Constants.CallKeys.CHANNEL]}"
                    }

                    if (data.containsKey(Constants.CallKeys.IS_VIDEO_CALL)) {
                        isVideoCall = data[Constants.CallKeys.IS_VIDEO_CALL] == "true"
                    }

                    if (data.containsKey(Constants.CallKeys.CONTACT_ID)) {
                        contactId = data[Constants.CallKeys.CONTACT_ID]?.toInt() ?: 0
                    }

                    if (channel != "private-" && contactId != 0) {
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

                        service.putExtras(bundle)

                        context.startService(service)
                    }
                }
                /*if (app != null && !app.isAppVisible()) {
                    val jsonObject = JSONObject()
                    jsonObject.put(
                        Constants.CallKeys.CHANNEL,
                        data[Constants.CallKeys.CHANNEL]
                    )
                    jsonObject.put(
                        Constants.CallKeys.IS_VIDEO_CALL,
                        data[Constants.CallKeys.IS_VIDEO_CALL] == "true"
                    )
                    jsonObject.put(
                        Constants.CallKeys.CONTACT_ID,
                        data[Constants.CallKeys.CONTACT_ID]?.toInt() ?: 0
                    )

                    sharedPreferencesManager.putString(
                        PREF_PENDING_CALL,
                        jsonObject.toString()
                    )
                }*/
            }
        }
    }

    fun createCallNotification(
        channel: String,
        contactId: Int,
        isVideoCall: Boolean,
        context: Context
    ): Notification {

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
            fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationTitle = if (isVideoCall) {
            context.getString(R.string.text_incoming_secure_call)
        } else {
            context.getString(R.string.text_incoming_secure_video_call)
        }

        val notificationBuilder = Builder(
            context,
            context.getString(R.string.calls_channel_id)
        )
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentTitle(notificationTitle)
            .setPriority(PRIORITY_HIGH)
            .setCategory(CATEGORY_CALL)
            .addAction(
                getServiceNotificationAction(
                    context,
                    WebRTCCallService.ACTION_DENY_CALL,
                    R.drawable.ic_close_black_24,
                    R.string.text_hang_up_call,
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

        if (callActivityRestricted(context)) {
            // Use a full-screen intent only for the highest-priority alerts where you
            // have an associated activity that you would like to launch after the user
            // interacts with the notification. Also, if your app targets Android 10
            // or higher, you need to request the USE_FULL_SCREEN_INTENT permission in
            // order for the platform to invoke this notification.
            notificationBuilder.setFullScreenIntent(fullScreenPendingIntent, true)
            notificationBuilder.priority = PRIORITY_HIGH
            notificationBuilder.setCategory(CATEGORY_CALL)
        }

        return notificationBuilder.build()
    }

    fun getNotificationId(context: Context, type: Int): Int {
        return if (callActivityRestricted(context) && type == Constants.NotificationType.INCOMING_CALL.type) {
            NOTIFICATION_RINGING
        } else {
            NOTIFICATION
        }
    }

    private fun callActivityRestricted(context: Context): Boolean {
        return Build.VERSION.SDK_INT >= 29 && !(context as NapoleonApplication).isAppVisible()
    }

    private fun createNotificationChannel(context: Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = context.getString(R.string.default_notification_channel_id)
            val name = context.getString(R.string.default_notification_channel_id)
            val descriptionText = context.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            channel.setShowBadge(true)
            channel.lockscreenVisibility = PRIORITY_MAX
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createCallNotificationChannel(context: Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val (id: String, name) = context.getString(R.string.calls_channel_id) to
                    context.getString(R.string.calls_channel_name)
            val descriptionText = context.getString(R.string.calls_channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(id, name, importance).apply {
                description = descriptionText
                setShowBadge(true)
                lockscreenVisibility = PRIORITY_MAX
            }

            if (Build.VERSION.SDK_INT >= 29) {
                val soundUri = Settings.System.DEFAULT_RINGTONE_URI

                val audioAttributes: AudioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
                channel.setSound(soundUri, audioAttributes)
            }

            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

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

}