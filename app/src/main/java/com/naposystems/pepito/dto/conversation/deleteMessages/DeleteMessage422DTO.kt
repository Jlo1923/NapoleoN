package com.naposystems.pepito.dto.conversation.deleteMessages

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class DeleteMessage422DTO(
    @Json(name = "user_receiver") val userReceiver: List<String> = ArrayList(),
    @Json(name = "messages_id") val messagesId: List<String> = ArrayList()
)