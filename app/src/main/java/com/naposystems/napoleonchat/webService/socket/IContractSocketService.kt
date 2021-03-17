package com.naposystems.napoleonchat.webService.socket

import com.naposystems.napoleonchat.source.remote.dto.newMessageEvent.NewMessageDataEventRes
import com.pusher.client.channel.PresenceChannel
import org.json.JSONObject

interface IContractSocketService {

    interface SocketService {

        fun initSocket()

        fun connectToSocketReadyForCall(channel: String)

        fun disconnectSocket()

        fun validatePusher()

        fun subscribe(jsonObject: String)

        fun subscribeToCallChannel(channel: String, isActionAnswer: Boolean, isVideoCall: Boolean)

        fun subscribeToCallChannelFromBackground(channel: String)

        fun joinToCall(channel: String)

        fun unSubscribeCallChannel(channelName: String)

        fun emitToCall(channel: String, jsonObject: JSONObject)

        fun emitToCall(channel: String, eventType: Int)

        fun getPusherChannel(channel: String): PresenceChannel?

        fun emitToClientConversation(jsonObject: String)
    }

    interface Repository {

        suspend fun getContacts()

        fun getUser(): Int

        fun getMyMessages(contactId: Int?)

        fun deleteContact(contactId: Int?)

        fun verifyMessagesReceived()

        fun verifyMessagesRead()

        fun getDeletedMessages()

        fun existIdMessage(id: String): Boolean

        fun rejectCall(contactId: Int, channel: String)

        fun readyForCall(contactId: Int, isVideoCall: Boolean, channelPrivate: String)

        fun insertNewMessage(newMessageDataEventRes: NewMessageDataEventRes)

        fun validateMessageType(messagesWebIds: List<String>, state: Int)

        fun updateMessagesStatus(messagesWebIds: List<String>, state: Int)

        fun notifyMessageReceived(messageId: String)
    }

}