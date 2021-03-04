package com.naposystems.napoleonchat.dto.conversation.message

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class MessageUnprocessableEntityDTO(
    @Json(name = "user_receiver") val userDestination: List<String> = ArrayList()
)