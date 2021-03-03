package com.naposystems.napoleonchat.ui.registerRecoveryAccountQuestion

import com.naposystems.napoleonchat.dto.registerRecoveryAccountQuestion.getQuestions.RegisterRecoveryAccountQuestionResDTO
import com.naposystems.napoleonchat.dto.registerRecoveryAccountQuestion.sendAnswers.RegisterRecoveryAccountReqDTO
import com.naposystems.napoleonchat.entity.RecoveryAnswer
import okhttp3.ResponseBody
import retrofit2.Response

interface IContractRegisterRecoveryAccountQuestion {
    interface ViewModel {
        fun addRecoveryAnswer(answer: RecoveryAnswer, isFinal: Int)
        fun getQuestions()
        fun sendRecoveryAnswers()
    }

    interface Repository {
        suspend fun getQuestions(): Response<List<RegisterRecoveryAccountQuestionResDTO>>
        suspend fun sendRecoveryAnswers(registerRecoveryAccountReqDTO: RegisterRecoveryAccountReqDTO): Response<Any>
        fun registeredQuestionsPref()
        fun getUnprocessableEntityError(response:ResponseBody) :ArrayList<String>
        fun getError(response: ResponseBody): ArrayList<String>
    }
}