package com.naposystems.napoleonchat.source.remote.dto.conversation.socket

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class AuthReqDTO(
    @Json(name = "headers") val headers: HeadersReqDTO
)