package com.naposystems.napoleonchat.source.remote.dto.conversation.message

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class MessageErrorDTO(
    @Json(name = "error") val error: String
)