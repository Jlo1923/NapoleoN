package com.naposystems.napoleonchat.source.remote.dto.registerRecoveryAccountQuestion.sendAnswers

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RegisterRecoveryAccountReqDTO(
    @Json(name = "questions") val questions: List<RegisterRecoveryAccountAnswerReqDTO>
)