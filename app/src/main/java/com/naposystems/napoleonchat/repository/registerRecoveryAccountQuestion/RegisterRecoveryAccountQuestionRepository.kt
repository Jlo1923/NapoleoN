package com.naposystems.napoleonchat.repository.registerRecoveryAccountQuestion

import com.naposystems.napoleonchat.source.remote.api.NapoleonApi
import com.naposystems.napoleonchat.source.remote.dto.recoveryAccountQuestions.sendAnswers.RegisterRecoveryAccountUnprocessableEntityDTO
import com.naposystems.napoleonchat.source.remote.dto.registerRecoveryAccountQuestion.getQuestions.RegisterRecoveryAccountQuestionErrorDTO
import com.naposystems.napoleonchat.source.remote.dto.registerRecoveryAccountQuestion.getQuestions.RegisterRecoveryAccountQuestionResDTO
import com.naposystems.napoleonchat.source.remote.dto.registerRecoveryAccountQuestion.sendAnswers.RegisterRecoveryAccountReqDTO
import com.naposystems.napoleonchat.ui.registerRecoveryAccountQuestion.IContractRegisterRecoveryAccountQuestion
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.utility.WebServiceUtils
import com.squareup.moshi.Moshi
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject

class RegisterRecoveryAccountQuestionRepository @Inject constructor(
    private val napoleonApi: NapoleonApi,
    private val sharedPreferencesManager: SharedPreferencesManager
) : IContractRegisterRecoveryAccountQuestion.Repository {

    private val moshi: Moshi by lazy {
        Moshi.Builder().build()
    }

    override suspend fun getQuestions(): Response<List<RegisterRecoveryAccountQuestionResDTO>> {
        return napoleonApi.getQuestions()
    }

    override fun getUnprocessableEntityError(response: ResponseBody): ArrayList<String> {
        val adapter = moshi.adapter(RegisterRecoveryAccountUnprocessableEntityDTO::class.java)

        val error = adapter.fromJson(response.string())

        return WebServiceUtils.getUnprocessableEntityErrors(error!!)
    }

    override fun getError(response: ResponseBody): ArrayList<String> {

        val adapter = moshi.adapter(RegisterRecoveryAccountQuestionErrorDTO::class.java)
        val error = adapter.fromJson(response.string())
        val errorList = ArrayList<String>()

        errorList.add(error!!.error)
        return errorList
    }

    override suspend fun sendRecoveryAnswers(registerRecoveryAccountReqDTO: RegisterRecoveryAccountReqDTO): Response<Any> {
        return napoleonApi.sendRecoveryQuestions(registerRecoveryAccountReqDTO)
    }

    override fun registeredQuestionsPref() {
        sharedPreferencesManager.putInt(
            Constants.SharedPreferences.PREF_RECOVERY_QUESTIONS_SAVED,
            Constants.RecoveryQuestionsSaved.SAVED_QUESTIONS.id
        )
    }
}