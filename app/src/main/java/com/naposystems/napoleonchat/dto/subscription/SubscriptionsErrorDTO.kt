package com.naposystems.napoleonchat.dto.subscription

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class SubscriptionsErrorDTO (
    @Json(name = "error") val error: String
)