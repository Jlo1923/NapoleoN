package com.naposystems.pepito.dto.recoveryOlderAccountQuestions.getQuestions

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RecoveryOlderAccountQuestionsErrorDTO (
    @Json(name = "error") val error: String
)