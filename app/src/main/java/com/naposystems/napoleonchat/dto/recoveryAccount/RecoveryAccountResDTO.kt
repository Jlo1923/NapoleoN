package com.naposystems.napoleonchat.dto.recoveryAccount

import com.naposystems.napoleonchat.model.recoveryAccount.RecoveryQuestions
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RecoveryAccountResDTO(
    @Json(name = "id") val questionId: Int,
    @Json(name = "question") val question: String,
    @Json(name = "answer") val answer: List<String>
) {
    companion object {
        fun toListRecoveryQuestions(listRecoveryDTO: List<RecoveryAccountResDTO>): List<RecoveryQuestions> {
            val listRecoveryQuestions: MutableList<RecoveryQuestions> = ArrayList()

            for (recoveryQuestion in listRecoveryDTO) {
                listRecoveryQuestions.add(
                    RecoveryQuestions(
                        recoveryQuestion.questionId,
                        recoveryQuestion.question,
                        recoveryQuestion.answer
                    )
                )
            }
            return listRecoveryQuestions
        }
    }
}

