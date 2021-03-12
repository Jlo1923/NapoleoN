package com.naposystems.napoleonchat.service.notification

import com.google.firebase.messaging.RemoteMessage

interface NotificationService {
    fun createInformativeNotification(
        dataFromNotification: Map<String, String>,
        notification: RemoteMessage.Notification?
    )
}