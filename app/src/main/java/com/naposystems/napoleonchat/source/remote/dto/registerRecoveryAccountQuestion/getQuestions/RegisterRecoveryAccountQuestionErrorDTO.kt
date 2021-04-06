package com.naposystems.napoleonchat.source.remote.dto.registerRecoveryAccountQuestion.getQuestions

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RegisterRecoveryAccountQuestionErrorDTO (
    @Json(name = "error") val error: String
)