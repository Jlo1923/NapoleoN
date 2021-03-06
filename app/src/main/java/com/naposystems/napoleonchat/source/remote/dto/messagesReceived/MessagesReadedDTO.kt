package com.naposystems.napoleonchat.source.remote.dto.messagesReceived

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MessagesReadedDTO(
    @Json(name = "data") val data: MessageReadedDataDTO
)

@JsonClass(generateAdapter = true)
data class MessageReadedDataDTO(
    @Json(name = "messages_id") val messageIds: List<String> = emptyList()
)