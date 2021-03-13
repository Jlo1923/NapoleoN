package com.naposystems.napoleonchat.service.socketOutAppMessage

import com.naposystems.napoleonchat.source.remote.dto.validateMessageEvent.ValidateMessage
import com.pusher.client.connection.ConnectionState

interface SocketOutAppMessageService {

    fun getStatusSocket(): ConnectionState?

    fun getStatusGlobalChannel(): Boolean

    fun connectSocket()

    fun emitClientConversation(messages: List<ValidateMessage>)

}