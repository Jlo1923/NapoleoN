package com.naposystems.napoleonchat.ui.registerRecoveryAccountQuestion

import com.naposystems.napoleonchat.source.remote.dto.registerRecoveryAccountQuestion.getQuestions.RegisterRecoveryAccountQuestionResDTO
import com.naposystems.napoleonchat.source.remote.dto.registerRecoveryAccountQuestion.sendAnswers.RegisterRecoveryAccountReqDTO
import com.naposystems.napoleonchat.model.RecoveryAnswer
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
        fun get422Error(response:ResponseBody) :ArrayList<String>
        fun getError(response: ResponseBody): ArrayList<String>
    }
}