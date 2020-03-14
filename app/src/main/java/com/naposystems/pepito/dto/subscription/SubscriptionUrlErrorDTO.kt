package com.naposystems.pepito.dto.subscription

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SubscriptionUrlErrorDTO (
    @Json(name = "error") val error: String
)