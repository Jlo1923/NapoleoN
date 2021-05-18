package com.naposystems.napoleonchat.service.download.notification

import android.app.Notification
import android.content.Context

interface NotificationDownloadClient {

    fun createDownloadNotification(
        context: Context,
        messageId: Int
    ): Notification

    fun updateDownloadNotificationProgress(
        max: Int,
        progress: Int,
        messageId: Int
    )

    fun cancelNotification(
        id: Int
    )

}