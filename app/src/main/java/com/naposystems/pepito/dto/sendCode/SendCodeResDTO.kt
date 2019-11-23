package com.naposystems.pepito.dto.sendCode

import com.squareup.moshi.Json

data class SendCodeResDTO(
    val id: String,
    @Json(name = "firebase_id")
    val firebaseId: String,
    val code: String
)