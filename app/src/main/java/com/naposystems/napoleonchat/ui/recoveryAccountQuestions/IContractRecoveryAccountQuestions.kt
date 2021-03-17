package com.naposystems.napoleonchat.ui.recoveryAccountQuestions

import com.naposystems.napoleonchat.source.remote.dto.recoveryAccountQuestions.RecoveryAccountAnswersDTO
import com.naposystems.napoleonchat.source.remote.dto.recoveryAccountQuestions.RecoveryAccountQuestionsResDTO
import com.naposystems.napoleonchat.source.remote.dto.recoveryAccountQuestions.RecoveryAccountUserDTO
import com.naposystems.napoleonchat.model.recoveryAccountQuestions.RecoveryAccountAnswers
import okhttp3.ResponseBody
import retrofit2.Response

interface IContractRecoveryAccountQuestions {
    interface ViewModel {
        fun sendRecoveryAnswers(nickname: String)
        fun addRecoveryAnswer(answer: RecoveryAccountAnswers)
        fun setFreeTrialPref(subscription: Boolean)
        fun setAttemptPref()
    }

    interface Repository {
        suspend fun sendRecoveryAnswers(nickname: String, answers: List<RecoveryAccountAnswersDTO>): Response<RecoveryAccountQuestionsResDTO>
        fun saveSecretKey(secretKey: String)
        fun setRecoveredAccountPref()
        fun setRecoveredQuestionsPref()
        suspend fun setAttemptPref()
        suspend fun setFreeTrialPref(subscription: Boolean)
        fun getUnprocessableEntityError(response: ResponseBody) :ArrayList<String>
        fun getError(response: ResponseBody): ArrayList<String>
        suspend fun insertUser(recoveryAccountUserDTO: RecoveryAccountUserDTO)

    }
}