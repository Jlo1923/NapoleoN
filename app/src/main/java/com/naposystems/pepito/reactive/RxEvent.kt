package com.naposystems.pepito.reactive

import org.json.JSONObject

class RxEvent {

    data class NewMessageReceivedEvent(val channelName: String, val jsonObject: JSONObject)
}