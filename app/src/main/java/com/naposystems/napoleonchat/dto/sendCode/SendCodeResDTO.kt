package com.naposystems.napoleonchat.dto.sendCode

import com.squareup.moshi.Json

data class SendCodeResDTO(
    @Json(name = "success") val success: Boolean
)