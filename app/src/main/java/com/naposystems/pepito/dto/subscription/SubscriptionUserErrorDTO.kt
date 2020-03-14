package com.naposystems.pepito.dto.subscription

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class SubscriptionUserErrorDTO (
    @Json(name = "error") val error: String
)