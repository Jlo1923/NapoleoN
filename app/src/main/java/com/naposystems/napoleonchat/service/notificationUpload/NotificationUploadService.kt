package com.naposystems.napoleonchat.service.notificationUpload

import android.app.Notification
import android.content.Context

interface NotificationUploadService {
    fun createUploadNotification(context: Context): Notification
    fun updateUploadNotificationProgress(max: Int, progress: Int)
}