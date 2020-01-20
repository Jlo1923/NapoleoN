package com.naposystems.pepito.dto.enterCode

import com.squareup.moshi.Json

data class EnterCodeResDTO(
    @Json(name = "its_ok") val itsOk: Boolean
)