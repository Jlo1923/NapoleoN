package com.naposystems.napoleonchat.source.remote.dto.accessPin

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateAccountErrorDTO(
    @Json(name = "error") val error: String
)