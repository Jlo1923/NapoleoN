package com.naposystems.napoleonchat.repository.registerRecoveryAccountQuestion

import com.naposystems.napoleonchat.source.remote.dto.registerRecoveryAccountQuestion.getQuestions.RegisterRecoveryAccountQuestionResDTO
import com.naposystems.napoleonchat.source.remote.dto.registerRecoveryAccountQuestion.sendAnswers.RegisterRecoveryAccountReqDTO
import okhttp3.ResponseBody
import retrofit2.Response

interface RegisterRecoveryAccountQuestionRepository {
    suspend fun getQuestions(): Response<List<RegisterRecoveryAccountQuestionResDTO>>
    suspend fun sendRecoveryAnswers(registerRecoveryAccountReqDTO: RegisterRecoveryAccountReqDTO): Response<Any>
    fun registeredQuestionsPref()
    fun getUnprocessableEntityError(response: ResponseBody): ArrayList<String>
    fun getError(response: ResponseBody): ArrayList<String>
}