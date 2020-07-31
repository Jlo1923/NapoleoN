package com.naposystems.pepito.dto.subscription

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StateSubscriptionResDTO(
    @Json(name = "state") val state: String
)