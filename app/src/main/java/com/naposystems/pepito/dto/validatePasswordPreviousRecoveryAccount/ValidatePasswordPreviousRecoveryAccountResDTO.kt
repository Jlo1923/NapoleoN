package com.naposystems.pepito.dto.validatePasswordPreviousRecoveryAccount

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ValidatePasswordPreviousRecoveryAccountResDTO (
    @Json(name = "success") val success: Boolean
)