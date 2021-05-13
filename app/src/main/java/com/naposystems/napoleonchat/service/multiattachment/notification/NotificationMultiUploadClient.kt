package com.naposystems.napoleonchat.service.multiattachment.notification

import android.app.Notification
import android.content.Context

interface NotificationMultiUploadClient {

    fun createUploadNotification(
        context: Context,
        id: Int
    ): Notification

    fun updateUploadNotificationProgress(
        max: Int,
        progress: Int,
        id: Int
    )

    fun cancelNotification(
        id: Int
    )
}