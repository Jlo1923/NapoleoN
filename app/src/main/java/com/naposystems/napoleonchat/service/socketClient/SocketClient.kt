package com.naposystems.napoleonchat.service.socketClient

import com.naposystems.napoleonchat.model.CallModel
import com.naposystems.napoleonchat.source.remote.dto.messagesReceived.MessagesReqDTO
import com.naposystems.napoleonchat.source.remote.dto.validateMessageEvent.ValidateMessage
import com.pusher.client.connection.ConnectionState
import org.json.JSONObject

interface SocketClient {

    fun setSocketEventListener(socketEventListener: SocketEventListener)

    fun getStatusSocket(): ConnectionState

    fun getStatusGlobalChannel(): Boolean

    fun connectSocket(mustSubscribeToPresenceChannel: Boolean = false, callModel: CallModel? = null)

    fun subscribeToPresenceChannel(callModel: CallModel)

    fun disconnectSocket(channelPresenceName: String = "")

    fun unSubscribePresenceChannel(channelName: String)

    fun emitClientConversation(messages: List<ValidateMessage>)

    fun emitClientConversation(messages: MessagesReqDTO)

    fun emitClientCall(channel: String, jsonObject: JSONObject)

    fun emitClientCall(channel: String, eventType: Int)

}