package com.naposystems.napoleonchat.source.remote.dto.registerRecoveryAccountQuestion.getQuestions

import com.naposystems.napoleonchat.model.Questions
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RegisterRecoveryAccountQuestionResDTO(
    @Json(name = "id") val id: Int,
    @Json(name = "question") val question: String
) {

    companion object {
        fun toListQuestionEntity(listRecoveryQuestion: List<RegisterRecoveryAccountQuestionResDTO>): List<Questions> {
            val listQuestion: MutableList<Questions> = ArrayList()

            for (recoveryQuestion in listRecoveryQuestion) {
                listQuestion.add(
                    toQuestionEntity(
                        recoveryQuestion
                    )
                )
            }

            return listQuestion
        }

        private fun toQuestionEntity(recoveryAccountQuestionResDTO: RegisterRecoveryAccountQuestionResDTO): Questions {
            return Questions(
                recoveryAccountQuestionResDTO.id,
                recoveryAccountQuestionResDTO.question
            )
        }
    }
}