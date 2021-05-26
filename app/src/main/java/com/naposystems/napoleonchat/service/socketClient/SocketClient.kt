package com.naposystems.napoleonchat.service.socketClient

import com.naposystems.napoleonchat.source.remote.dto.messagesReceived.MessagesReqDTO
import com.naposystems.napoleonchat.source.remote.dto.validateMessageEvent.ValidateMessage
import com.pusher.client.connection.ConnectionState
import org.json.JSONObject

interface SocketClient {

    fun setEventsFromSocketClientListener(eventsFromSocketClientListener: EventsFromSocketClientListener)

    fun getStatusSocket(): ConnectionState

    fun getStatusGlobalChannel(): Boolean

    suspend fun connectSocket()

    suspend fun subscribeToPresenceChannel()

    fun disconnectSocket()

    fun unSubscribePresenceChannel()

    fun emitClientConversation(messages: List<ValidateMessage>)

    fun emitClientConversation(messages: MessagesReqDTO)

    fun emitClientCall(jsonObject: JSONObject)

    fun emitClientCall(eventType: Int)

    fun isConnected(): Boolean

}