package com.naposystems.napoleonchat.service.notificationClient

import android.app.Notification
import com.google.firebase.messaging.RemoteMessage
import java.util.*

interface HandlerNotification {

    fun showNotification(
        dataFromNotification: Map<String, String>,
        notification: RemoteMessage.Notification?,
        notificationId: Int = Random().nextInt()
    )

    fun createNotificationCallBuilder(): Notification

    fun notificationCallInProgress()

}