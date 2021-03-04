package com.naposystems.napoleonchat.source.remote.dto.conversation.socket

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class HeadersReqDTO(
    @Json(name = "X-API-Key") val key: String
)