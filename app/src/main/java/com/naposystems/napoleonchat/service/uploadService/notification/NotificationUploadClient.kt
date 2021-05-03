package com.naposystems.napoleonchat.service.uploadService.notification

import android.app.Notification
import android.content.Context

interface NotificationUploadClient {
    fun createUploadNotification(context: Context): Notification
    fun updateUploadNotificationProgress(max: Int, progress: Int)
}