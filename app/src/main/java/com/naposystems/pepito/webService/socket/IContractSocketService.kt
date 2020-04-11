package com.naposystems.pepito.webService.socket

import org.json.JSONObject

interface IContractSocketService {

    interface SocketService {

        fun initSocket()

        fun subscribe(jsonObject: JSONObject)

        fun subscribeToCallChannel(channel: String, jsonObject: JSONObject)

        fun joinToCall(channel: String)

        fun unSubscribe(jsonObject: JSONObject, channelName: String)

        fun emitToCall(channel: String, jsonObject: JSONObject)

        fun emitToCall(channel: String, eventType: Int)
    }

    interface Repository {

        fun getMyMessages()

        fun verifyMessagesReceived()

        fun verifyMessagesRead()

        fun getDeletedMessages()

        fun rejectCall(contactId: Int, channel: String)
    }

}