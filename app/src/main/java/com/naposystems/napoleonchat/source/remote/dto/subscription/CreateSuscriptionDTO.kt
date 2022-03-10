package com.naposystems.napoleonchat.source.remote.dto.subscription

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateSuscriptionDTO(
    @Json(name = "user_id") val user_id : String,
    @Json(name = "subscription_id") val subscription_id: String
)
