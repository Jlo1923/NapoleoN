package com.naposystems.napoleonchat.service.socketClient

import com.naposystems.napoleonchat.model.CallModel
import com.naposystems.napoleonchat.source.remote.dto.validateMessageEvent.ValidateMessage
import com.pusher.client.connection.ConnectionState
import org.json.JSONObject

interface SocketClient {

    fun setSocketEventListener(socketEventListenerCall: SocketEventListener)

    fun getStatusSocket(): ConnectionState

    fun getStatusGlobalChannel(): Boolean

    fun connectSocket(mustSubscribeToPresenceChannel: Boolean = false, callModel: CallModel? = null)

    fun subscribeToPresenceChannel(callModel: CallModel)
    
    fun disconnectSocket()

    fun unSubscribePresenceChannel(channelName: String)

    fun emitClientConversation(messages: List<ValidateMessage>)

    fun emitClientCall(channel: String, jsonObject: JSONObject)

    fun emitClientCall(channel: String, eventType: Int)

}