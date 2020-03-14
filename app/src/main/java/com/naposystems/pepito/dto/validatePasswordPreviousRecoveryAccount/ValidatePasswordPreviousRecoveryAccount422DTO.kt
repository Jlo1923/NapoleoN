package com.naposystems.pepito.dto.validatePasswordPreviousRecoveryAccount

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ValidatePasswordPreviousRecoveryAccount422DTO (
    @Json(name = "firebase_id") val firebaseId: String,
    @Json(name = "nick") val nickname: String,
    @Json(name = "password") val password: String
)