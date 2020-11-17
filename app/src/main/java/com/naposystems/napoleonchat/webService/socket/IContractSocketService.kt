package com.naposystems.napoleonchat.webService.socket

import com.pusher.client.channel.PresenceChannel
import com.pusher.client.channel.PrivateChannel
import org.json.JSONObject

interface IContractSocketService {

    interface SocketService {

        fun initSocket()

        fun connectToSocketReadyForCall(channel: String)

        fun disconnectSocket()

        fun subscribe(jsonObject: String)

        fun subscribeToCallChannel(channel: String, isActionAnswer: Boolean, isVideoCall: Boolean)

        fun subscribeToCallChannelFromBackground(channel: String)

        fun joinToCall(channel: String)

        fun unSubscribeCallChannel(channelName: String)

        fun emitToCall(channel: String, jsonObject: JSONObject)

        fun emitToCall(channel: String, eventType: Int)

        fun getPusherChannel(channel: String): PresenceChannel?
    }

    interface Repository {

        suspend fun getContacts()

        fun getMyMessages(contactId: Int?)

        fun deleteContact(contactId: Int?)

        fun verifyMessagesReceived()

        fun verifyMessagesRead()

        fun getDeletedMessages()

        fun rejectCall(contactId: Int, channel: String)

        fun readyForCall(contactId: Int, isVideoCall: Boolean, channelPrivate: String)
    }

}