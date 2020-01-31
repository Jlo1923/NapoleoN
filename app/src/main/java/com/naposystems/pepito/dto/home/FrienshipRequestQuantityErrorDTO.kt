package com.naposystems.pepito.dto.home

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FrienshipRequestQuantityErrorDTO(
    @Json(name = "error") val error: String
)