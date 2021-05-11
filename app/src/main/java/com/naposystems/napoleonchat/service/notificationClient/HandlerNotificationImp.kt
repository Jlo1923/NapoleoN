package com.naposystems.napoleonchat.service.notificationClient

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.RemoteMessage
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.app.NapoleonApplication
import com.naposystems.napoleonchat.service.syncManager.SyncManager
import com.naposystems.napoleonchat.ui.conversationCall.ConversationCallActivity
import com.naposystems.napoleonchat.ui.mainActivity.MainActivity
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utils.handlerMediPlayer.HandlerMediaPlayerNotification
import com.naposystems.napoleonchat.utils.handlerNotificationChannel.HandlerNotificationChannel
import com.naposystems.napoleonchat.webRTC.service.WebRTCService
import timber.log.Timber
import javax.inject.Inject


class HandlerNotificationImp
@Inject constructor(
    private val context: Context,
    private val handlerNotificationChannel: HandlerNotificationChannel,
    private val handlerMediaPlayerNotification: HandlerMediaPlayerNotification,
    private val syncManager: SyncManager,
) : HandlerNotification {

    companion object {
        const val NOTIFICATION_CALL_ACTIVE = 171087
        const val NOTIFICATION_RINGING = 950707
        const val NOTIFICATION_UPLOADING = 20102020
        const val NOTIFICATION = 162511

        const val SUMMARY_ID = 12345678
        const val GROUP_MESSAGE = "GROUP_MESSAGE"

//        val mediaPlayer: MediaPlayer = MediaPlayer()
    }

    override fun showNotification(
        dataFromNotification: Map<String, String>,
        notification: RemoteMessage.Notification?,
        notificationId: Int
    ) {
        with(NotificationManagerCompat.from(context)) {
            notify(
                notificationId,
                createNotificationMessageBuilder(dataFromNotification, notification).build()
            )
        }
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
                handlerNotificationChannel.getChannelType(
                    dataFromNotification.getValue(Constants.NotificationKeys.TYPE_NOTIFICATION)
                        .toInt(),
                    dataFromNotification.getValue(Constants.NotificationKeys.CONTACT).toInt()
                )
            } else {
                handlerNotificationChannel.getChannelType(
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

    private fun createPendingIntent(
        dataFromNotification: Map<String, String>
    ): PendingIntent? {

        Timber.d("**Paso 10.2 : Crear Pending Intent data: $dataFromNotification")

        val intent = Intent(context, MainActivity::class.java)

        intent.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
        )

        with(dataFromNotification) {

            if (this.isNotEmpty()) {

                if (this.containsKey(Constants.NotificationKeys.CONTACT)) {
                    intent.putExtra(
                        Constants.NotificationKeys.CONTACT,
                        this.getValue(Constants.NotificationKeys.CONTACT).toString()
                    )
                }

                if (this.containsKey(Constants.NotificationKeys.MESSAGE_ID)) {
                    intent.putExtra(
                        Constants.NotificationKeys.MESSAGE_ID,
                        this.getValue(Constants.NotificationKeys.MESSAGE_ID).toString()
                    )
                }

                if (this.containsKey(Constants.NotificationKeys.ATTACKER_ID)) {
                    intent.putExtra(
                        Constants.NotificationKeys.ATTACKER_ID,
                        this.getValue(Constants.NotificationKeys.ATTACKER_ID).toString()
                    )
                }

            }

        }

        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_ONE_SHOT
        )
    }

    override fun createNotificationCallBuilder(): Notification {

        Timber.d("LLAMADA PASO: createNotificationCallBuilder")

        val contact = NapoleonApplication.callModel?.let { syncManager.getContact(it.contactId) }

        val notificationBuilder = NotificationCompat.Builder(
            context,
            context.getString(if (NapoleonApplication.isVisible) R.string.alerts_channel_id else R.string.calls_channel_id)
        ).apply {
            setSmallIcon(R.drawable.ic_call_black_24)
            setGroup(context.getString(R.string.calls_group_key))
            setContentTitle("@${contact?.getNickName()}")
            setContentText(NapoleonApplication.callModel?.let {
                getTexNotification(
                    it.typeCall,
                    it.isVideoCall
                )
            })
            setCategory(NotificationCompat.CATEGORY_CALL)
            priority = NotificationCompat.PRIORITY_MAX
            setOngoing(true)
            when (NapoleonApplication.callModel?.typeCall) {
                Constants.TypeCall.IS_INCOMING_CALL -> {
                    addAction(
                        getServiceNotificationAction(
                            WebRTCService.ACTION_DENY_CALL,
                            R.drawable.ic_close_black_24,
                            R.string.text_reject
                        )
                    )
                    addAction(
                        getServiceNotificationAction(
                            WebRTCService.ACTION_ANSWER_CALL,
                            R.drawable.ic_call_black_24,
                            R.string.text_answer_call
                        )
                    )
                }
                Constants.TypeCall.IS_OUTGOING_CALL -> {
                    addAction(
                        getServiceNotificationAction(
                            WebRTCService.ACTION_HANG_UP,
                            R.drawable.ic_close_black_24,
                            R.string.text_hang_up_call
                        )
                    )
                }
            }
        }

        val intent = Intent(context, WebRTCService::class.java).apply {
            this.action = WebRTCService.ACTION_OPEN_CALL
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent =
            PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        notificationBuilder.apply {
            setFullScreenIntent(pendingIntent, true)
        }

        if (NapoleonApplication.callModel?.typeCall == Constants.TypeCall.IS_INCOMING_CALL && NapoleonApplication.isVisible.not()) {
            Timber.d("RINGTONE: PlayRingtone EN HANDLER NOTIFICATION")
            handlerMediaPlayerNotification.playRingtone()
        }

        return notificationBuilder.build()
    }

    override fun notificationCallInProgress() {

        val intent = Intent(context, ConversationCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(
            context,
            context.getString(R.string.alerts_channel_id)
        ).apply {
            setGroup(context.getString(R.string.calls_group_key))
            setSmallIcon(R.drawable.ic_call_black_24)
            setUsesChronometer(true)
            setContentTitle(context.getString(R.string.text_call_in_progress))
            setOngoing(true)
            if (Build.VERSION.SDK_INT >= 29) {
                setFullScreenIntent(pendingIntent, true)
                priority = NotificationCompat.PRIORITY_MAX
            }
            addAction(
                getServiceNotificationAction(
                    WebRTCService.ACTION_HANG_UP,
                    R.drawable.ic_close_black_24,
                    R.string.text_hang_up_call
                )
            )
        }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(NOTIFICATION_CALL_ACTIVE, notificationBuilder.build())
    }

    private fun getServiceNotificationAction(
        action: String,
        iconResId: Int,
        titleResId: Int
    ): NotificationCompat.Action {

        val intent = Intent(context, WebRTCService::class.java).apply {
            this.action = action
        }

        val pendingIntent =
            PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        return NotificationCompat.Action(
            iconResId,
            context.getString(titleResId),
            pendingIntent
        )
    }

    private fun getTexNotification(typeCall: Constants.TypeCall, isVideoCall: Boolean): String {

        return if (typeCall == Constants.TypeCall.IS_INCOMING_CALL) {
            if (isVideoCall.not())
                context.getString(R.string.text_incoming_secure_call)
            else
                context.getString(R.string.text_incoming_secure_video_call)
        } else {
            if (isVideoCall.not())
                context.getString(R.string.text_secure_outgoing_call)
            else
                context.getString(R.string.text_secure_outgoing_video_call)
        }
    }
}