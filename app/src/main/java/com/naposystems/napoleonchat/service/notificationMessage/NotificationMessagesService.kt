package com.naposystems.napoleonchat.service.notificationMessage

import com.google.firebase.messaging.RemoteMessage

interface NotificationMessagesService {
    fun createInformativeNotification(
        dataFromNotification: Map<String, String>,
        notification: RemoteMessage.Notification?
    )
}