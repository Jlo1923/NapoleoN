package com.naposystems.napoleonchat.dto.recoveryOlderAccountQuestions.sendAnswers

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RecoveryOlderAccountQuestionsAnswers422DTO (
    @Json(name = "nick") val nickname: String,
    @Json(name = "firstanswer") val firstAnswer: String,
    @Json(name = "secondanswer") val secondAnswer: String,
    @Json(name = "firebase_id") val firebaseId: String
)