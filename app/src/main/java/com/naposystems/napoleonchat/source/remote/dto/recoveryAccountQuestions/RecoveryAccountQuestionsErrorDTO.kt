package com.naposystems.napoleonchat.source.remote.dto.recoveryAccountQuestions

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RecoveryAccountQuestionsErrorDTO(
    @Json(name = "error") val error: String
)