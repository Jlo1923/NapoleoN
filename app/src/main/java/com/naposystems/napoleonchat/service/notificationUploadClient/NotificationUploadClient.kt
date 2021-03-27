package com.naposystems.napoleonchat.service.notificationUploadClient

import android.app.Notification
import android.content.Context

interface NotificationUploadClient {
    fun createUploadNotification(context: Context): Notification
    fun updateUploadNotificationProgress(max: Int, progress: Int)
}