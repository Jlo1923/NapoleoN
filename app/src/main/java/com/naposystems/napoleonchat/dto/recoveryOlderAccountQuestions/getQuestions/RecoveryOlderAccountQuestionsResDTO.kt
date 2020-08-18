package com.naposystems.napoleonchat.dto.recoveryOlderAccountQuestions.getQuestions

import com.naposystems.napoleonchat.model.recoveryOlderAccount.RecoveryOlderAccountQuestions
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RecoveryOlderAccountQuestionsResDTO(
    @Json(name = "first_question_id") val firstQuestionId: Int,
    @Json(name = "first_question") val firstQuestion: String,
    @Json(name = "second_question_id") val secondQuestionId: Int,
    @Json(name = "second_question") val secondQuestion: String
) {
    companion object {
        fun model(response: RecoveryOlderAccountQuestionsResDTO): RecoveryOlderAccountQuestions {
            return RecoveryOlderAccountQuestions(
                firstQuestionId = response.firstQuestionId,
                firstQuestion = response.firstQuestion,
                secondQuestionId = response.secondQuestionId,
                secondQuestion = response.secondQuestion
            )
        }
    }
}