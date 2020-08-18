package com.naposystems.napoleonchat.dto.conversation.message

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class Message422DTO(
    @Json(name = "user_receiver") val userDestination: List<String> = ArrayList()
)