package com.naposystems.napoleonchat.ui.recoveryOlderAccountQuestions

import com.naposystems.napoleonchat.dto.recoveryOlderAccountQuestions.RecoveryOlderAccountDTO
import com.naposystems.napoleonchat.dto.recoveryOlderAccountQuestions.getQuestions.RecoveryOlderAccountQuestionsResDTO
import com.naposystems.napoleonchat.dto.recoveryOlderAccountQuestions.sendAnswers.RecoveryOlderAccountQuestionsAnswersResDTO
import okhttp3.ResponseBody
import retrofit2.Response

interface IContractRecoveryOlderAccountQuestions {
    interface ViewModel {
        fun getOlderQuestions(nickname: String)
        fun sendAnswers(nickname: String, answerOne: String, answerTwo: String)
        fun resetRecoveryQuestions()
        fun setAttemptPref()
    }

    interface Repository {
        suspend fun getOlderQuestions(nickname: String): Response<RecoveryOlderAccountQuestionsResDTO>
        suspend fun sendAnswers(nickname: String, answerOne: String, answerTwo: String): Response<RecoveryOlderAccountQuestionsAnswersResDTO>
        suspend fun insertUser(recoveryOlderAccountDTO: RecoveryOlderAccountDTO)
        fun setRecoveredAccountPref()
        suspend fun setAttemptPref()
        suspend fun get422Error(response: ResponseBody): ArrayList<String>
        suspend fun getDefaultQuestionsError(response: Response<RecoveryOlderAccountQuestionsResDTO>): ArrayList<String>
        suspend fun getDefaultAnswersError(response: Response<RecoveryOlderAccountQuestionsAnswersResDTO>): ArrayList<String>
        fun saveSecretKey(secretKey: String)
    }
}