package com.naposystems.pepito.dto.conversation.message

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class ConversationErrorDTO(
    @Json(name = "error") val error: String
)