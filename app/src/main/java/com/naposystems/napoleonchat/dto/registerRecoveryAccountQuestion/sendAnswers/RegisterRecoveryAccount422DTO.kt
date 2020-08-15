package com.naposystems.napoleonchat.dto.registerRecoveryAccountQuestion.sendAnswers

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RegisterRecoveryAccount422DTO(
    @Json(name = "questions") val questions: List<String> = ArrayList()
)