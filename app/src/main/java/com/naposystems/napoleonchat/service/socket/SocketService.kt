package com.naposystems.napoleonchat.service.socket

import com.naposystems.napoleonchat.source.remote.dto.validateMessageEvent.ValidateMessage
import com.pusher.client.channel.PresenceChannel
import org.json.JSONObject

interface SocketService {

    fun connectSocket(locationConnectSocket: Boolean)

    fun disconnectSocket()

    fun connectToSocketReadyForCall(channel: String)

    fun subscribeToCallChannel(channel: String, isActionAnswer: Boolean, isVideoCall: Boolean)

    fun joinToCall(channel: String)

    fun unSubscribeCallChannel(channelName: String)

    fun emitToCall(channel: String, jsonObject: JSONObject)

    fun emitToCall(channel: String, eventType: Int)

    fun getPusherChannel(channel: String): PresenceChannel?

//        fun subscribeToCallChannelFromBackground(channel: String)

    fun emitClientConversation(messages: List<ValidateMessage>)
    fun connectSocket(locationConnectSocket: Boolean, socketCallback: SocketCallback?)
}