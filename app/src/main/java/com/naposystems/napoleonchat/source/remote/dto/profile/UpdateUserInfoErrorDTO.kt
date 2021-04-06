package com.naposystems.napoleonchat.source.remote.dto.profile

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UpdateUserInfoErrorDTO(
    @Json(name = "error") val error: String
)