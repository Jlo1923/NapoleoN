package com.naposystems.napoleonchat.service.uploadService.notification

import android.app.Notification
import android.content.Context

interface NotificationUploadClient {

    fun createUploadNotification(context: Context, id: Int): Notification

    fun updateUploadNotificationProgress(
        max: Int,
        progress: Int,
        id: Int?
    )
}