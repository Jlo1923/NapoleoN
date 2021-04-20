package com.naposystems.napoleonchat.service.download.notification

import android.app.Notification
import android.content.Context

interface NotificationDownloadClient {

    fun createDownloadNotification(
        context: Context
    ): Notification

    fun updateDownloadNotificationProgress(
        max: Int,
        progress: Int
    )

}