package com.naposystems.napoleonchat.model.recoveryAccountQuestions

import com.naposystems.napoleonchat.source.remote.dto.recoveryAccountQuestions.RecoveryAccountAnswersDTO

data class RecoveryAccountAnswers(
    val questionId: Int,
    val answer: String
) {
    companion object {
        fun toListRecoveryAccountAnswersReqDTO(
            listRecoveryAnswers: List<RecoveryAccountAnswers>
        ): List<RecoveryAccountAnswersDTO> {
            val answers: MutableList<RecoveryAccountAnswersDTO> = ArrayList()

            for (recoveryAnswer in listRecoveryAnswers) {
                answers.add(
                    RecoveryAccountAnswersDTO(
                        recoveryAnswer.questionId,
                        recoveryAnswer.answer
                    )
                )
            }

            return answers
        }
    }
}