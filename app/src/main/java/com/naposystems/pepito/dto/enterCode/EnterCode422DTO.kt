package com.naposystems.pepito.dto.enterCode

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EnterCode422DTO(
    @Json(name = "firebase_id") val firebaseId: List<String>,
    @Json(name = "code_verification") val codeVerification: List<String>
)