package com.naposystems.napoleonchat.dto.enterCode

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EnterCodeUnprocessableEntityDTO(
    @Json(name = "firebase_id") val firebaseId: List<String>,
    @Json(name = "code_verification") val codeVerification: List<String>
)