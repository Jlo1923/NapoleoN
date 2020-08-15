package com.naposystems.napoleonchat.model.conversationCall

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class IncomingCallData(
    @Json(name = "channel_private") val channel: String,
    @Json(name = "contact_id") val contactId: Int,
    @Json(name = "is_videocall") val isVideoCall: Boolean
)