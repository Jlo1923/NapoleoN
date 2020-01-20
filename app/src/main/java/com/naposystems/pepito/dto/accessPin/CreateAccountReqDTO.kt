package com.naposystems.pepito.dto.accessPin

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateAccountReqDTO(
    @Json(name = "firebase_id") val firebaseId: String,
    @Json(name = "fullname") val fullName: String,
    @Json(name = "nick") val nickname: String,
    @Json(name = "language_iso") val languageIso: String,
    @Json(name = "password") val accessPin: String,
    @Json(name = "password_confirmation") val confirmAccessPin: String,
    @Json(name = "my_status") val status: String
)