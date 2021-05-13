package com.naposystems.napoleonchat.service.multiattachment.notification

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

class NotificationMultiUploadClientImpl
@Inject constructor(
    private val context: Context
) : NotificationMultiUploadClient {

    companion object {
        const val NOTIFICATION_UPLOADING_MULTI = 20102021
    }

    override fun createUploadNotification(
        context: Context,
        id: Int
    ): Notification {
        val text = context.getString(R.string.text_sending_file_multiple) + """ ${id} """

        createNotificationChannel(id.toString())

        Log.i("Jkdev notification", text)
        val notificationBuilder = NotificationCompat.Builder(
            context,
            id.toString()
        ).setSmallIcon(R.drawable.ic_file_upload_black)
            .setContentTitle(context.getString(R.string.text_sending_file_multiple))
            .setContentText(text)
            .setProgress(0, 0, true)
            .setOngoing(true)

        return notificationBuilder.build()
    }


    override fun updateUploadNotificationProgress(max: Int, progress: Int, id: Int) {
        val text = context.getString(R.string.text_sending_file_multiple) + """ ${id} """
        Log.i("Jkdev update", text)
        val notificationBuilder = NotificationCompat.Builder(
            context,
            id.toString()
        )
            .setSmallIcon(R.drawable.ic_file_upload_black)
            .setContentTitle(context.getString(R.string.text_sending_file_multiple))
            .setContentText(context.getString(R.string.text_sending_file_multiple))
            .setProgress(max, progress, false)
            .setOngoing(true)

        val notification = notificationBuilder.build()

        val mNotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        mNotificationManager.notify(id, notification)

    }

    override fun cancelNotification(id: Int) {
        Log.i("Jkdev cancelNotification", "$id")
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