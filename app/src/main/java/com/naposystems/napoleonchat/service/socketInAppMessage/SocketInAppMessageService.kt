package com.naposystems.napoleonchat.service.socketInAppMessage

import com.naposystems.napoleonchat.source.remote.dto.validateMessageEvent.ValidateMessage
import com.pusher.client.channel.PresenceChannel
import org.json.JSONObject

interface SocketInAppMessageService {

    //region Conexion
    fun getPusherChannel(channel: String): PresenceChannel?

    fun connectSocket()

    fun disconnectSocket()
    //endregion

    //region Mensajes
    fun emitClientConversation(messages: List<ValidateMessage>)
    //endregion

    //region llamadas
    fun connectToSocketReadyForCall(channel: String)

    fun subscribeToCallChannel(channel: String, isActionAnswer: Boolean, isVideoCall: Boolean)

    fun joinToCall(channel: String)

    fun emitToCall(channel: String, jsonObject: JSONObject)

    fun emitToCall(channel: String, eventType: Int)

    fun unSubscribeCallChannel(channelName: String)
    //endregion
}