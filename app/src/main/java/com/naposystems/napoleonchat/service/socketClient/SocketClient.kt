package com.naposystems.napoleonchat.service.socketClient

import com.naposystems.napoleonchat.model.CallModel
import com.naposystems.napoleonchat.source.remote.dto.validateMessageEvent.ValidateMessage
import com.pusher.client.connection.ConnectionState
import org.json.JSONObject

interface SocketClient {

    fun setSocketCallListener(socketEventsListenerCall: SocketEventsListener.Call)

    fun setSocketCallOutAppListener(socketEventsListenerCallOutApp: SocketEventsListener.CallOutApp)

    fun getStatusSocket(): ConnectionState

    fun getStatusGlobalChannel(): Boolean

    fun getStatusPresenceChannel(channel: String): Boolean

    fun connectSocket(mustSubscribeToPresenceChannel: Boolean = false, callModel: CallModel? = null)

    fun disconnectSocket()

    fun unSubscribePresenceChannel(channelName: String)

    fun emitClientConversation(messages: List<ValidateMessage>)

    fun emitClientCall(channel: String, jsonObject: JSONObject)

    fun emitClientCall(channel: String, eventType: Int)

    fun subscribeToPresenceChannel(callModel: CallModel)

}