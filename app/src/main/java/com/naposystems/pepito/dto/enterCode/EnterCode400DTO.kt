package com.naposystems.pepito.dto.enterCode

import com.squareup.moshi.Json

data class EnterCode400DTO(
    @Json(name = "error") val error: String
)