package com.naposystems.napoleonchat.service.download.notification

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.naposystems.napoleonchat.R
import javax.inject.Inject

const val NOTIFICATION_DOWNLOADING_MULTI = 20102022

class NotificationDownloadClientImp
@Inject constructor(
    private val context: Context
) : NotificationDownloadClient {

    override fun createDownloadNotification(
        context: Context
    ): Notification {
        val notificationBuilder = NotificationCompat.Builder(
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

    override fun updateDownloadNotificationProgress(max: Int, progress: Int) {
        val notificationBuilder = NotificationCompat.Builder(
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
        mNotificationManager.notify(NOTIFICATION_DOWNLOADING_MULTI, notification)
    }

}