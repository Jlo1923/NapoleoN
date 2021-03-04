package com.naposystems.napoleonchat.source.remote.dto.conversation.call

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CallContactReqDTO(
    @Json(name = "user_receiver") val contactToCall: Int,
    @Json(name = "is_videocall") val isVideoCall: Boolean
)