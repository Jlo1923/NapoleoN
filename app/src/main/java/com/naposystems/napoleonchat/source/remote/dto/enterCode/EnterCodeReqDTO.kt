package com.naposystems.napoleonchat.source.remote.dto.enterCode

import com.squareup.moshi.Json

data class EnterCodeReqDTO(
    @Json(name = "firebase_id") val firebaseId: String,
    @Json(name = "code_verification") val codeVerification: String
)