package com.naposystems.napoleonchat.service.notificationMessage

import android.app.Notification
import com.google.firebase.messaging.RemoteMessage

interface NotificationMessagesService {

    fun createNotification(
        dataFromNotification: Map<String, String>,
        notification: RemoteMessage.Notification?
    )

    fun updateCallInProgress(channel: String, contactId: Int, isVideoCall: Boolean)

    fun startWebRTCCallService(
        channel: String,
        isVideoCall: Boolean,
        contactId: Int,
        isIncomingCall: Boolean,
        offer: String
    )

    fun createNotificationCallBuilder(
        channel: String,
        contactId: Int,
        isVideoCall: Boolean,
        typeCall: Int,
        offer: String = ""
    ): Notification

    fun stopMediaPlayer()
}