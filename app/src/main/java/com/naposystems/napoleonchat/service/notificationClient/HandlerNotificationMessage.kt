package com.naposystems.napoleonchat.service.notificationClient

import com.google.firebase.messaging.RemoteMessage

interface HandlerNotificationMessage {

    fun handlerMessage(
        dataFromNotification: Map<String, String>,
        notification: RemoteMessage.Notification?
    )

}