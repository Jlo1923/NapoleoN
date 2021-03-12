package com.naposystems.napoleonchat.service.notification

import com.google.firebase.messaging.RemoteMessage

interface NotificationService {
    fun createInformativeNotification(
        data: Map<String, String>,
        notification: RemoteMessage.Notification?
    )
}