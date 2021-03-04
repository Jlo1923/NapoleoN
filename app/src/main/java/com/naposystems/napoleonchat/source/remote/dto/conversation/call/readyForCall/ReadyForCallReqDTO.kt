package com.naposystems.napoleonchat.source.remote.dto.conversation.call.readyForCall

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReadyForCallReqDTO(
    @Json(name = "contact_id") val contactId: Int,
    @Json(name = "is_videocall") val isVideoCall: Boolean,
    @Json(name = "channel_private") val channelPrivate: String,
)