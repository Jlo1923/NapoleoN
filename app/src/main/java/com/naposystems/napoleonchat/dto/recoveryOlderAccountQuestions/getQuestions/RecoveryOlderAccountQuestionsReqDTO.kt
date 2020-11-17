package com.naposystems.napoleonchat.dto.recoveryOlderAccountQuestions.getQuestions

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RecoveryOlderAccountQuestionsReqDTO (
    @Json(name = "nick") val nickname: String
)