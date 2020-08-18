package com.naposystems.napoleonchat.entity

import com.naposystems.napoleonchat.dto.registerRecoveryAccountQuestion.sendAnswers.RegisterRecoveryAccountAnswerReqDTO

data class RecoveryAnswer(
    val questionId: Int,
    val answer: String
) {
    companion object {

        fun toListRegisterRecoveryAccountAnswerReqDTO(
            listRecoveryAnswer: List<RecoveryAnswer>
        ): List<RegisterRecoveryAccountAnswerReqDTO> {

            val answers: MutableList<RegisterRecoveryAccountAnswerReqDTO> = ArrayList()

            for (recoveryAnswer in listRecoveryAnswer) {
                answers.add(
                    RegisterRecoveryAccountAnswerReqDTO(
                        recoveryAnswer.questionId,
                        recoveryAnswer.answer
                    )
                )
            }

            return answers
        }
    }
}