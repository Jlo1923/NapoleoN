package com.naposystems.napoleonchat.source.remote.dto.messagesReceived

import com.naposystems.napoleonchat.utility.Constants
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
data class MessagesReceivedRESDTO(
    @Json(name = "data") val data: MessagesResDTO
)

@JsonClass(generateAdapter = true)
data class MessagesReadedRESDTO(
    @Json(name = "data") val data: MessagesResDTO
)

@JsonClass(generateAdapter = true)
data class MessageAndAttachmentResDTO(
    @Json(name = "messages_id") val messagesId: List<String>,
    @Json(name = "attachments_id") val attachmentsId: List<String>,
)

@JsonClass(generateAdapter = true)
data class MessageReceivedResDTO(
    @Json(name = "success") val success: Boolean
)

@JsonClass(generateAdapter = true)
data class MessagesReqDTO(
    @Json(name = "messages") val messages: List<MessageDTO> = emptyList()
)


@JsonClass(generateAdapter = true)
data class MessagesResDTO(
    @Json(name = "messages") val messages: List<MessageDTO> = emptyList()
)

@JsonClass(generateAdapter = true)
data class MessageDTO(
    @Json(name = "message_id") val id: String,
    @Json(name = "type") val type: Int,
    @Json(name = "user") val user: Int?,
    @Json(name = "status") val status: Int?
) {
    fun isUnread(): Boolean {
        return this.status == Constants.MessageEventType.UNREAD.status
    }

    fun isTypeText(): Boolean {
        return this.type == Constants.MessageType.TEXT.type
    }

}