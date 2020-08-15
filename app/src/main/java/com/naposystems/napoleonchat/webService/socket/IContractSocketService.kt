package com.naposystems.napoleonchat.webService.socket

import org.json.JSONObject

interface IContractSocketService {

    interface SocketService {

        fun initSocket()

        fun subscribe(jsonObject: String)

        fun subscribeToCallChannel(channel: String)

        fun subscribeToCallChannelFromBackground(channel: String)

        fun joinToCall(channel: String)

        fun unSubscribeCallChannel(channelName: String)

        fun emitToCall(channel: String, jsonObject: JSONObject)

        fun emitToCall(channel: String, eventType: Int)
    }

    interface Repository {

        suspend fun getContacts()

        fun getMyMessages()

        fun verifyMessagesReceived()

        fun verifyMessagesRead()

        fun getDeletedMessages()

        fun rejectCall(contactId: Int, channel: String)
    }

}