package com.naposystems.napoleonchat.source.remote.dto.enterCode

import com.squareup.moshi.Json

data class EnterCodeResDTO(
    @Json(name = "its_ok") val success: Boolean
)