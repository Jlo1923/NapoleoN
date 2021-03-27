package com.naposystems.napoleonchat.service.notificationClient

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
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.RemoteMessage
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.app.NapoleonApplication
import com.naposystems.napoleonchat.model.CallModel
import com.naposystems.napoleonchat.service.syncManager.SyncManager
import com.naposystems.napoleonchat.ui.conversationCall.ConversationCallActivity
import com.naposystems.napoleonchat.ui.mainActivity.MainActivity
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Utils
import com.naposystems.napoleonchat.utils.handlerNotificationChannel.HandlerNotificationChannel
import com.naposystems.napoleonchat.webRTC.service.WebRTCService
import timber.log.Timber
import javax.inject.Inject

class HandlerNotificationImp
@Inject constructor(
    private val context: Context,
    private val handlerNotificationChannel: HandlerNotificationChannel,
    private val napoleonApplication: NapoleonApplication,
    private val syncManager: SyncManager,
) : HandlerNotification {

    companion object {
        const val NOTIFICATION_RINGING = 950707
        const val NOTIFICATION_UPLOADING = 20102020
        const val NOTIFICATION = 162511

        const val SUMMARY_ID = 12345678
        const val GROUP_MESSAGE = "GROUP_MESSAGE"

        val mediaPlayer: MediaPlayer = MediaPlayer()
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

    override fun createNotificationCallBuilder(
        callModel: CallModel
    ): Notification {

        val contact = syncManager.getContact(callModel.contactId)

        val notificationBuilder = NotificationCompat.Builder(
            context,
            context.getString(if (napoleonApplication.visible) R.string.alerts_channel_id else R.string.calls_channel_id)
        ).apply {
            setSmallIcon(R.drawable.ic_call_black_24)
            setGroup(context.getString(R.string.calls_group_key))
            setContentTitle("@${contact?.getNickName()}")
            setContentText(getTexNotification(callModel.typeCall, callModel.isVideoCall))
            setCategory(NotificationCompat.CATEGORY_CALL)
            setOngoing(true)
            if (callModel.typeCall == Constants.TypeCall.IS_INCOMING_CALL) {
                addAction(
                    getServiceNotificationAction(
                        WebRTCService.ACTION_DENY_CALL,
                        R.drawable.ic_close_black_24,
                        R.string.text_reject,
                        callModel
                    )
                )
                addAction(
                    getServiceNotificationAction(
                        WebRTCService.ACTION_ANSWER_CALL,
                        R.drawable.ic_call_black_24,
                        R.string.text_answer_call,
                        callModel
                    )
                )
            } else {
                addAction(
                    getServiceNotificationAction(
                        WebRTCService.ACTION_HANG_UP,
                        R.drawable.ic_close_black_24,
                        R.string.text_hang_up_call,
                        callModel
                    )
                )
            }

        }

        if (callModel.typeCall == Constants.TypeCall.IS_INCOMING_CALL) {

            val fullScreenIntent =
                Intent(context, ConversationCallActivity::class.java).apply {
                    putExtras(Bundle().apply {
                        callModel.typeCall = Constants.TypeCall.IS_INCOMING_CALL
                        putSerializable(ConversationCallActivity.CALL_MODEL,callModel)
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

    override fun notificationCallInProgress(callModel: CallModel) {

        val notificationBuilder = NotificationCompat.Builder(
            context,
            context.getString(R.string.alerts_channel_id)
        ).apply {
            setGroup(context.getString(R.string.calls_group_key))
            setSmallIcon(R.drawable.ic_call_black_24)
            setUsesChronometer(true)
            setContentTitle(context.getString(R.string.text_call_in_progress))
            setOngoing(true)
            addAction(
                getServiceNotificationAction(
                    WebRTCService.ACTION_HANG_UP,
                    R.drawable.ic_close_black_24,
                    R.string.text_hang_up_call,
                    callModel
                )
            )
        }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(NOTIFICATION_RINGING, notificationBuilder.build())
    }

    private fun getServiceNotificationAction(
        action: String,
        iconResId: Int,
        titleResId: Int,
        callModel: CallModel
    ): NotificationCompat.Action {

        val intent = Intent(context, WebRTCService::class.java).apply {
            this.action = action
            putExtras(Bundle().apply {
                putSerializable(Constants.CallKeys.CALL_MODEL, callModel)
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

    private fun getTexNotification(typeCall: Constants.TypeCall, isVideoCall: Boolean): String {

        return if (typeCall == Constants.TypeCall.IS_INCOMING_CALL) {
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

}