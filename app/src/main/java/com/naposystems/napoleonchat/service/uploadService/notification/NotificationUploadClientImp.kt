package com.naposystems.napoleonchat.service.uploadService.notification

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.naposystems.napoleonchat.R
import javax.inject.Inject

class NotificationUploadClientImp
@Inject constructor(
    private val context: Context
) : NotificationUploadClient {

    companion object {
        const val NOTIFICATION_UPLOADING = 20102020
        const val NOTIFICATION_UPLOADING_MULTI = 20102021
    }

    override fun createUploadNotification(
        context: Context,
        id: Int
    ): Notification {
        val text = context.getString(R.string.text_sending_file) + """ ${id} """
        val notificationBuilder = NotificationCompat.Builder(
            context,
            context.getString(R.string.alerts_channel_id)
        )
            .setSmallIcon(R.drawable.ic_file_upload_black)
            .setContentTitle(context.getString(R.string.text_sending_file))
            .setContentText(text)
            .setProgress(0, 0, true)
            .setOngoing(true)

        return notificationBuilder.build()
    }

    override fun updateUploadNotificationProgress(max: Int, progress: Int, id: Int?) {
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
        id?.let {
            mNotificationManager.notify(it, notification)
        } ?: kotlin.run {
            mNotificationManager.notify(NOTIFICATION_UPLOADING, notification)
        }

    }

}