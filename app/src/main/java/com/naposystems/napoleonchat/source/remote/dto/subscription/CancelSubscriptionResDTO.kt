package com.naposystems.napoleonchat.source.remote.dto.subscription

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CancelSubscriptionResDTO(
    @Json(name = "success") val success: Boolean
)