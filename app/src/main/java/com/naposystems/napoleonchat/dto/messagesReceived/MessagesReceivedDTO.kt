package com.naposystems.napoleonchat.dto.messagesReceived

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MessagesReceivedDTO(
    @Json(name = "data") val data: MessageReceivedDataDTO
)

@JsonClass(generateAdapter = true)
data class MessageReceivedDataDTO(
    @Json(name = "messages_id") val messageIds: List<String> = emptyList()
)