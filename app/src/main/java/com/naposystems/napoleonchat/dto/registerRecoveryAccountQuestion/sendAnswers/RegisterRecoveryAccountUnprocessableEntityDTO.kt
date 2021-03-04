package com.naposystems.napoleonchat.dto.registerRecoveryAccountQuestion.sendAnswers

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RegisterRecoveryAccountUnprocessableEntityDTO(
    @Json(name = "questions") val questions: List<String> = ArrayList()
)