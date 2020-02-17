package com.naposystems.pepito.dto.muteConversation

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MuteConversationReqDTO(
    @Json(name = "time") val time: Int = 0,
    @Json(name = "type") val type: Int = 0
)