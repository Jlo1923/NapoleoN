package com.naposystems.napoleonchat.source.remote.dto.conversation.call.readyForCall

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReadyForCallResDTO(
    @Json(name = "success") val success: Boolean
)