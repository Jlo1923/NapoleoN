package com.naposystems.napoleonchat.source.remote.dto.cancelCall

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CancelCallResDTO(
    @Json(name = "success") val success: Boolean
)