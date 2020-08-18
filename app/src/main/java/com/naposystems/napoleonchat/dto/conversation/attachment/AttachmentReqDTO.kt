package com.naposystems.napoleonchat.dto.conversation.attachment

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AttachmentReqDTO(
    @Json(name = "message_id") val messageId: String,
    @Json(name = "body") val body: String,
    @Json(name = "type") val type: String
)