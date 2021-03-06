package com.naposystems.napoleonchat.source.remote.dto.sendCode

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SendCodeErrorDTO(
    @Json(name = "error") val error: String
)