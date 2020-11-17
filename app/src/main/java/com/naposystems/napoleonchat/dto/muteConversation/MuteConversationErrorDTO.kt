package com.naposystems.napoleonchat.dto.muteConversation

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MuteConversationErrorDTO(
    @Json(name = "error") val error: String
)