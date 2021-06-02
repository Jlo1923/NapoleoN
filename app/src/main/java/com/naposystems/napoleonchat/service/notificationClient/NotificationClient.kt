package com.naposystems.napoleonchat.service.notificationClient

import android.app.Notification
import com.google.firebase.messaging.RemoteMessage

interface NotificationClient {

    fun createNotification(
        dataFromNotification: Map<String, String>,
        notification: RemoteMessage.Notification?
    )
}