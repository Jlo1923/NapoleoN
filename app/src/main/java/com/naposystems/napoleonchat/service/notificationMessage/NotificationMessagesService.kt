package com.naposystems.napoleonchat.service.notificationMessage

import com.google.firebase.messaging.RemoteMessage

interface NotificationMessagesService {

    fun createInformativeNotification(
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

    fun stopMediaPlayer()

}