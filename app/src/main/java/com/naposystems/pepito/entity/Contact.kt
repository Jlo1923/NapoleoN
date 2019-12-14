package com.naposystems.pepito.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Contact(
    @Json(name = "image_url") val imageUrl: String,
    @Json(name = "nickname") val nickname: String,
    @Json(name = "display_name") val displayName: String,
    @Json(name = "status") val status: String,
    @Json(name = "last_seen") val lastSeen: String
)