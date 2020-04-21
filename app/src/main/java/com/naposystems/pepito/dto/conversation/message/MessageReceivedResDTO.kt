package com.naposystems.pepito.dto.conversation.message

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MessageReceivedResDTO(
    @Json(name = "success") val success: Boolean
)