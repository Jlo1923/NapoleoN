package com.naposystems.napoleonchat.dto.enterCode

import com.squareup.moshi.Json

data class EnterCodeResDTO(
    @Json(name = "its_ok") val success: Boolean
)