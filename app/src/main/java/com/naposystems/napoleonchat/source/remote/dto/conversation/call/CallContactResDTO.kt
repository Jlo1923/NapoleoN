package com.naposystems.napoleonchat.source.remote.dto.conversation.call

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CallContactResDTO(
    @Json(name = "channel") val channel: String
)