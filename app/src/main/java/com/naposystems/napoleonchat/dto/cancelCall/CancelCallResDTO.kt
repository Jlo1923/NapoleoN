package com.naposystems.napoleonchat.dto.cancelCall

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CancelCallResDTO(
    @Json(name = "success") val success: Boolean
)