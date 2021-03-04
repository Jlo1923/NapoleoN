package com.naposystems.napoleonchat.source.remote.dto.muteConversation

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MuteConversationErrorDTO(
    @Json(name = "error") val error: String
)