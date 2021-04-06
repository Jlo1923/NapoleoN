package com.naposystems.napoleonchat.source.remote.dto.home

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FrienshipRequestQuantityErrorDTO(
    @Json(name = "error") val error: String
)