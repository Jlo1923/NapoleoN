package com.naposystems.napoleonchat.dto.conversation.message

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MessageReceivedReqDTO(
    @Json(name = "message_id") val messageId: String
)