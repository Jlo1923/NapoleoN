package com.naposystems.napoleonchat.source.remote.dto.muteConversation

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MuteConversationResDTO(
    @Json(name = "success") val success: Boolean
)