package com.naposystems.napoleonchat.model.conversationCall

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class IncomingCall(
    @Json(name = "data") val data: IncomingCallData
)