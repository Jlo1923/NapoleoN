package com.naposystems.napoleonchat.dto.validatePasswordPreviousRecoveryAccount

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ValidatePasswordPreviousRecoveryAccountReqDTO (
    @Json(name = "nick") val nickname: String,
    @Json(name = "firebase_id") val firebaseId: String,
    @Json(name = "password") val password: String
)