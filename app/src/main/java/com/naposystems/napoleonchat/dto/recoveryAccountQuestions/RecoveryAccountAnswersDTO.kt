package com.naposystems.napoleonchat.dto.recoveryAccountQuestions

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RecoveryAccountAnswersDTO(
    @Json(name = "question_id") val questionId: Int,
    @Json(name = "answer") val answer: String
)