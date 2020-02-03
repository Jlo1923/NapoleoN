package com.naposystems.pepito.dto.conversation.message

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class Message422DTO(
    @Json(name = "user_destination") val userDestination: List<String> = ArrayList(),
    @Json(name = "type") val type: List<String> = ArrayList(),
    @Json(name = "body") val body: List<String> = ArrayList()
)