package com.naposystems.napoleonchat.dto.recoveryOlderAccountQuestions.sendAnswers

import com.naposystems.napoleonchat.dto.recoveryOlderAccountQuestions.RecoveryOlderAccountDTO
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RecoveryOlderAccountQuestionsAnswersResDTO(
    @Json(name = "success") val success: Boolean,
    @Json(name = "user") val user: RecoveryOlderAccountDTO
)