package com.naposystems.pepito.dto.conversation.message

import com.naposystems.pepito.entity.conversation.ConversationAttachment
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ConversationAttachmentReqDTO(
    @Json(name = "type") val type: String,
    @Json(name = "body") val body: String
)