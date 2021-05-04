package com.naposystems.napoleonchat.service.download.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.naposystems.napoleonchat.R
import timber.log.Timber
import javax.inject.Inject

const val NOTIFICATION_DOWNLOADING_MULTI = 20102022

class NotificationDownloadClientImp
@Inject constructor(
    private val context: Context
) : NotificationDownloadClient {

    override fun createDownloadNotification(
        context: Context,
        messageId: Int
    ): Notification {

        Log.i("Jkdev download service", "bajando $messageId")

        createNotificationChannel(messageId.toString())

        val notificationBuilder = NotificationCompat.Builder(
            context,
            context.getString(R.string.alerts_channel_id)
        )
            .setSmallIcon(R.drawable.ic_file_download_black)
            .setContentTitle(context.getString(R.string.text_downloading_file))
            .setContentText(context.getString(R.string.text_downloading_file))
            .setProgress(0, 0, true)
            .setOngoing(true)

        return notificationBuilder.build()
    }

    override fun updateDownloadNotificationProgress(max: Int, progress: Int, messageId: Int) {

        Log.i("Jkdev updateando service", "bajando $messageId")

        val notificationBuilder = NotificationCompat.Builder(
            context,
            context.getString(R.string.alerts_channel_id)
        )
            .setSmallIcon(R.drawable.ic_file_download_black)
            .setContentTitle(context.getString(R.string.text_sending_file))
            .setContentText(context.getString(R.string.text_sending_file))
            .setProgress(max, progress, false)
            .setOngoing(true)

        val notification = notificationBuilder.build()

        val mNotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.notify(NOTIFICATION_DOWNLOADING_MULTI, notification)
    }

    override fun cancelNotification(id: Int) {

        Log.i("Jkdev cancelled service", "cancelled $id")

        val notificationManager =
            ContextCompat.getSystemService(
                context,
                NotificationManager::class.java
            ) as NotificationManager
        notificationManager.cancel(id)
    }

    private fun createNotificationChannel(toString: String) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Timber.d("*NotificationTest: createNotificationChannel")

            val channelId = toString
            val name = context.getString(R.string.default_notification_channel_id)
            val descriptionText = context.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_MIN
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            channel.setShowBadge(true)
            channel.lockscreenVisibility = NotificationCompat.PRIORITY_MIN
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

}