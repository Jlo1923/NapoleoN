package com.naposystems.napoleonchat.webService.socket

import com.naposystems.napoleonchat.source.remote.dto.newMessageEvent.NewMessageDataEventRes
import com.naposystems.napoleonchat.source.remote.dto.validateMessageEvent.ValidateMessage
import com.pusher.client.channel.PresenceChannel
import org.json.JSONObject

interface IContractSocketService {

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

        fun emitToClientConversation(jsonObject: String)

//        fun subscribeToCallChannelFromBackground(channel: String)


    }

    interface Repository {

        fun getUserId(): Int

        fun getMyMessages(contactId: Int?)

        fun verifyMessagesReceived()

        fun verifyMessagesRead()

        fun insertNewMessage(newMessageDataEventRes: NewMessageDataEventRes)

        fun notifyMessageReceived(messageId: String)

        fun updateMessagesStatus(messagesWebIds: List<String>, state: Int)

        fun getDeletedMessages()

        fun deleteContact(contactId: Int?)

        fun rejectCall(contactId: Int, channel: String)

        fun existIdMessage(id: String): Boolean

        fun validateMessageType(messagesWebIds: List<String>, state: Int)

//        fun readyForCall(contactId: Int, isVideoCall: Boolean, channelPrivate: String)

    }

}