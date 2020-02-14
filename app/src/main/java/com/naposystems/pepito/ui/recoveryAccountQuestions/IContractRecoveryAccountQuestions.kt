package com.naposystems.pepito.ui.recoveryAccountQuestions

import com.naposystems.pepito.dto.recoveryAccountQuestions.RecoveryAccountAnswersDTO
import com.naposystems.pepito.dto.recoveryAccountQuestions.RecoveryAccountQuestionsResDTO
import com.naposystems.pepito.dto.recoveryAccountQuestions.RecoveryAccountUserDTO
import com.naposystems.pepito.model.recoveryAccountQuestions.RecoveryAccountAnswers
import okhttp3.ResponseBody
import retrofit2.Response

interface IContractRecoveryAccountQuestions {
    interface ViewModel {
        fun sendRecoveryAnswers(nickname: String)
        fun addRecoveryAnswer(answer: RecoveryAccountAnswers)
    }

    interface Repository {
        suspend fun sendRecoveryAnswers(nickname: String, answers: List<RecoveryAccountAnswersDTO>): Response<RecoveryAccountQuestionsResDTO>
        fun saveRecoveredAccountPref()
        fun saveSecretKey(secretKey: String)
        fun get422Error(response: ResponseBody) :ArrayList<String>
        fun getError(response: ResponseBody): ArrayList<String>
        suspend fun insertUser(recoveryAccountUserDTO: RecoveryAccountUserDTO)
    }
}