package com.naposystems.pepito.dto.recoveryAccountQuestions

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RecoveryAccountQuestionsResDTO(
    @Json(name = "success") val success: Boolean,
    @Json(name = "user") val user: RecoveryAccountUserDTO
)