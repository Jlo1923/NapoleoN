package com.naposystems.napoleonchat.service.notification

import com.naposystems.napoleonchat.source.remote.dto.validateMessageEvent.ValidateMessage
import com.pusher.client.channel.PresenceChannel
import com.pusher.client.connection.ConnectionState
import org.json.JSONObject

interface SocketNotificationService {

    fun getStatusSocket(): ConnectionState?

    fun getStatusGlobalChannel(): Boolean

    fun connectSocket()

//    fun disconnectSocket()

    fun emitClientConversation(messages: List<ValidateMessage>)

}