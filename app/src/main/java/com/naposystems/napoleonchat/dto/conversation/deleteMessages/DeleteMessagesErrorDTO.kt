package com.naposystems.napoleonchat.dto.conversation.deleteMessages

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DeleteMessagesErrorDTO(
    @Json(name = "error") val error: String
)