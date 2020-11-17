package com.naposystems.napoleonchat.dto.recoveryAccountQuestions

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RecoveryAccountQuestions422DTO(
    @Json(name = "questions") val questions: List<String> = ArrayList(),
    @Json(name = "firebase_id") val firebaseId: List<String> = ArrayList()
)