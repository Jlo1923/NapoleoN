package com.naposystems.pepito.dto.recoveryOlderAccountQuestions.sendAnswers

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RecoveryOlderAccountQuestionsAnswersReqDTO (
    @Json(name = "nick") val nickname: String,
    @Json(name = "firebase_id") val firebaseId: String,
    @Json(name = "firstanswer") val firstAnswer: String,
    @Json(name = "secondanswer") val secondAnswer: String
)