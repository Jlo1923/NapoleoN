package com.naposystems.napoleonchat.source.remote.dto.conversation.message

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MessagesReadReqDTO(
    @Json(name = "messages_id") val messagesWebId: List<String>
)