package com.naposystems.napoleonchat.source.remote.dto.subscription

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LastSubscriptionDTO(
    @Json(name = "status") val status: Boolean,
    @Json(name = "expire") val expire: Long
)