package com.naposystems.napoleonchat.service.notificationClient

import android.app.Notification
import com.google.firebase.messaging.RemoteMessage
import com.naposystems.napoleonchat.model.CallModel
import java.util.*

interface HandlerNotification {

    fun showNotification(
        dataFromNotification: Map<String, String>,
        notification: RemoteMessage.Notification?,
        notificationId: Int = Random().nextInt()
    )

    fun createNotificationCallBuilder(
        callModel: CallModel
    ): Notification

    fun stopMediaPlayer()

    fun notificationCallInProgress(callModel: CallModel)

}