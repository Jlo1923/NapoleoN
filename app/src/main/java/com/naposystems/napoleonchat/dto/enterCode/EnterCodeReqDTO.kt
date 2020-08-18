package com.naposystems.napoleonchat.dto.enterCode

import com.squareup.moshi.Json

data class EnterCodeReqDTO(
    @Json(name = "firebase_id") val firebaseId: String,
    @Json(name = "code_verification") val codeVerification: String
)