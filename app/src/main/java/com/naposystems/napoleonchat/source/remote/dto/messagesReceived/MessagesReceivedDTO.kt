package com.naposystems.napoleonchat.source.remote.dto.messagesReceived

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

@JsonClass(generateAdapter = true)
data class MessagesReceivedREQDTO(
    @Json(name = "data") val data: MessagesReqDTO
)

@JsonClass(generateAdapter = true)
data class MessagesReqDTO(
    @Json(name = "messages") val messages: List<MessageDTO> = emptyList()
)

@JsonClass(generateAdapter = true)
data class MessagesReceivedRESDTO(
    @Json(name = "data") val data: MessagesResDTO
)

@JsonClass(generateAdapter = true)
data class MessagesResDTO(
    @Json(name = "messages") val messages: List<MessageDTO> = emptyList()
)

@JsonClass(generateAdapter = true)
data class MessageDTO(
    @Json(name = "message_id") val id: String,
    @Json(name = "type") val type: Int,
    @Json(name = "user") val userId: Int?,
    @Json(name = "status") val status: Int?
)