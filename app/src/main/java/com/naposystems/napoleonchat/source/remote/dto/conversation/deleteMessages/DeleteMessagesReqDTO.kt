package com.naposystems.napoleonchat.source.remote.dto.conversation.deleteMessages

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DeleteMessagesReqDTO(
    @Json(name = "user_receiver")
    val userReceiver: Int,
    @Json(name = "messages_id")
    val messagesId: List<String> = emptyList(),
    @Json(name = "attachments_id")
    val attachmentsId: List<String> = emptyList()
)