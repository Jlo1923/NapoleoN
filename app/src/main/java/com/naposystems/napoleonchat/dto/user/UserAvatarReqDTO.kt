package com.naposystems.napoleonchat.dto.user

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserAvatarReqDTO(
    @Json(name = "avatar") val avatar: String
)