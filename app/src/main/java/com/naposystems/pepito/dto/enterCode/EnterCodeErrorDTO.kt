package com.naposystems.pepito.dto.enterCode

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EnterCodeErrorDTO(
    @Json(name = "error") val error: String
)