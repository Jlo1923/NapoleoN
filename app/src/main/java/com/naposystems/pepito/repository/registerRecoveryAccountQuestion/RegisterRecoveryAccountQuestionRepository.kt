package com.naposystems.pepito.repository.registerRecoveryAccountQuestion

import com.naposystems.pepito.dto.registerRecoveryAccountQuestion.getQuestions.RegisterRecoveryAccountQuestionErrorDTO
import com.naposystems.pepito.dto.registerRecoveryAccountQuestion.getQuestions.RegisterRecoveryAccountQuestionResDTO
import com.naposystems.pepito.dto.registerRecoveryAccountQuestion.sendAnswers.RegisterRecoveryAccount422DTO
import com.naposystems.pepito.dto.registerRecoveryAccountQuestion.sendAnswers.RegisterRecoveryAccountReqDTO
import com.naposystems.pepito.ui.registerRecoveryAccountQuestion.IContractRegisterRecoveryAccountQuestion
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.utility.WebServiceUtils
import com.naposystems.pepito.webService.NapoleonApi
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

    override fun get422Error(response: ResponseBody): ArrayList<String> {
        val adapter = moshi.adapter(RegisterRecoveryAccount422DTO::class.java)

        val error = adapter.fromJson(response.string())

        return WebServiceUtils.get422Errors(error!!)
    }

    override fun getError(response: ResponseBody): ArrayList<String> {
        val moshi = Moshi.Builder().build()

        val adapter = moshi.adapter(RegisterRecoveryAccountQuestionErrorDTO::class.java)

        val error = adapter.fromJson(response.string())

        val errorList = ArrayList<String>()

        errorList.add(error!!.error)

        return errorList
    }

    override suspend fun sendRecoveryAnswers(registerRecoveryAccountReqDTO: RegisterRecoveryAccountReqDTO): Response<Any> {
        return napoleonApi.sendRecoveryQuestions(registerRecoveryAccountReqDTO)
    }

    override fun registeredQuestions() {
        sharedPreferencesManager.putInt(
            Constants.SharedPreferences.PREF_RECOVERY_QUESTIONS_SAVED,
            Constants.RecoveryQuestionsSaved.YES.option
        )
    }
}