package com.naposystems.napoleonchat.service.socket

import com.naposystems.napoleonchat.source.remote.dto.validateMessageEvent.ValidateMessage
import com.pusher.client.channel.PresenceChannel
import org.json.JSONObject

interface OLDSocketService {

    //region Conexion
    fun connectSocket()

    fun disconnectSocket()
    //endregion

    //region Mensajes
    fun emitClientConversation(messages: List<ValidateMessage>)
    //endregion

    fun connectToSocketReadyForCall(channel: String)

    fun subscribeToCallChannel(channel: String, isActionAnswer: Boolean, isVideoCall: Boolean)

    fun joinToCall(channel: String)

    fun unSubscribeCallChannel(channelName: String)

    fun emitToCall(channel: String, jsonObject: JSONObject)

    fun emitToCall(channel: String, eventType: Int)

    fun getPusherChannel(channel: String): PresenceChannel?

//        fun subscribeToCallChannelFromBackground(channel: String)

    fun getSocketId(): String
}