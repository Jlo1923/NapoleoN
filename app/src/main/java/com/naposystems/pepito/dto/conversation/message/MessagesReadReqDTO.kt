package com.naposystems.pepito.dto.conversation.message

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MessagesReadReqDTO(
    @Json(name = "messages_id") val messagesWebId: List<String>
)