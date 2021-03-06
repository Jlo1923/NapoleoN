package com.naposystems.napoleonchat.source.remote.dto.recoveryAccount

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RecoveryAccountErrorDTO(
    @Json(name = "error") val error: String
)