package com.naposystems.pepito.dto.registerRecoveryAccountQuestion.sendAnswers

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RegisterRecoveryAccountAnswerReqDTO(
    @Json(name = "question_id") val questionId: Int,
    @Json(name = "answer") val answer: String
)