package com.naposystems.pepito.webService.socket

import org.json.JSONObject

interface IContractSocketService {

    interface SocketService {

        fun subscribe(jsonObject: JSONObject)

        fun unSubscribe(jsonObject: JSONObject, channelName: String)
    }

    interface Repository {

        fun getMyMessages()

        fun verifyMessagesReceived()

        fun verifyMessagesRead()

        fun getDeletedMessages()
    }

}