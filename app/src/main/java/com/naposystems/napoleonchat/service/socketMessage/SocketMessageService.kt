package com.naposystems.napoleonchat.service.socketMessage

import com.naposystems.napoleonchat.source.remote.dto.validateMessageEvent.ValidateMessage
import com.pusher.client.channel.PresenceChannel
import com.pusher.client.connection.ConnectionState
import org.json.JSONObject

interface SocketMessageService {

    fun setSocketCallListener(socketEventsListenerCall: SocketEventsListener.Call)

    //region Conexion
    fun getPusherChannel(channel: String): PresenceChannel?

    fun connectSocket()

    fun disconnectSocket()
    //endregion

    //region Mensajes
    fun emitClientConversation(messages: List<ValidateMessage>)
    //endregion

    //region llamadas
    fun subscribeToCallChannel(
        contactId: Int,
        channel: String,
        isVideoCall: Boolean,
        offer: String = ""
    )

    fun joinToCall(channel: String)

    fun emitToCall(channel: String, jsonObject: JSONObject)

    fun emitToCall(channel: String, eventType: Int)

    fun unSubscribeCallChannel(channelName: String)

    //endregion
    fun getStatusSocket(): ConnectionState
    fun getStatusGlobalChannel(): Boolean
    fun subscribeToCallChannelFromBackground(channel: String)

}