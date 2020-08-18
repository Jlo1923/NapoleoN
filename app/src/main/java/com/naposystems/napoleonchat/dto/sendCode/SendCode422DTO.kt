package com.naposystems.napoleonchat.dto.sendCode

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SendCode422DTO(
    @Json(name = "firebase_id") val firebaseId: String
)