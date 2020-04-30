package com.naposystems.pepito.dto.conversation.message

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MessageReqDTO(
    @Json(name = "user_receiver") val userDestination: Int,
    @Json(name = "quoted") val quoted: String,
    @Json(name = "body") val body: String,
    @Json(name = "number_attachments") val numberAttachments: Int,
    @Json(name = "destroy") val destroy: Int,
    @Json(name = "type_message") val messageType: Int
)