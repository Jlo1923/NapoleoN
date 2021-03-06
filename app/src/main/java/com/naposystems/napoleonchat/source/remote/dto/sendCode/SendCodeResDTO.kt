package com.naposystems.napoleonchat.source.remote.dto.sendCode

import com.squareup.moshi.Json

data class SendCodeResDTO(
    @Json(name = "success") val success: Boolean
)