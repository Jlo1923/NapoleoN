package com.naposystems.napoleonchat.source.remote.dto.conversation.message

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MessageReqTestDTO(
    @Json(name = "user_receiver") val userDestination: Int,
    @Json(name = "quoted") val quoted: String,
    @Json(name = "body") val body: String,
    @Json(name = "type_attachment") val attachmentType: String
)