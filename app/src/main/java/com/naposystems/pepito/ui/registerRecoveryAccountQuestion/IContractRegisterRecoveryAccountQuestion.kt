package com.naposystems.pepito.ui.registerRecoveryAccountQuestion

import com.naposystems.pepito.dto.registerRecoveryAccountQuestion.getQuestions.RegisterRecoveryAccountQuestionResDTO
import com.naposystems.pepito.dto.registerRecoveryAccountQuestion.sendAnswers.RegisterRecoveryAccountReqDTO
import com.naposystems.pepito.entity.RecoveryAnswer
import okhttp3.ResponseBody
import retrofit2.Response

interface IContractRegisterRecoveryAccountQuestion {
    interface ViewModel {
        fun addRecoveryAnswer(answer: RecoveryAnswer)
        fun getQuestions()
        fun sendRecoveryAnswers()
    }

    interface Repository {
        suspend fun getQuestions(): Response<List<RegisterRecoveryAccountQuestionResDTO>>
        suspend fun sendRecoveryAnswers(registerRecoveryAccountReqDTO: RegisterRecoveryAccountReqDTO): Response<Any>
        fun registeredQuestionsPref()
        fun get422Error(response:ResponseBody) :ArrayList<String>
        fun getError(response: ResponseBody): ArrayList<String>
    }
}