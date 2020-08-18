package com.naposystems.napoleonchat.dto.recoveryAccountQuestions

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RecoveryAccountQuestionsReqDTO(
    @Json(name = "nick") val nick: String,
    @Json(name = "firebase_id") val firebaseID: String,
    @Json(name = "questions") val  questions: List<RecoveryAccountAnswersDTO>
)