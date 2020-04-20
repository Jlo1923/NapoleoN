package com.naposystems.pepito.dto.user

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserAvatarReqDTO(
    @Json(name = "avatar") val avatar: String
)