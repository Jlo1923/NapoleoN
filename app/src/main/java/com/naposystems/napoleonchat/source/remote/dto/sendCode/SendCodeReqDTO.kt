package com.naposystems.napoleonchat.source.remote.dto.sendCode

import com.squareup.moshi.Json

data class SendCodeReqDTO(
    @Json(name = "firebase_id") val firebaseId: String
)