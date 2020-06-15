package com.naposystems.pepito.repository.recoveryOlderAccountQuestionsRepository

import com.naposystems.pepito.BuildConfig
import com.naposystems.pepito.crypto.Crypto
import com.naposystems.pepito.db.dao.user.UserLocalDataSource
import com.naposystems.pepito.dto.recoveryOlderAccountQuestions.RecoveryOlderAccountDTO
import com.naposystems.pepito.dto.recoveryOlderAccountQuestions.getQuestions.RecoveryOlderAccountQuestionsErrorDTO
import com.naposystems.pepito.dto.recoveryOlderAccountQuestions.getQuestions.RecoveryOlderAccountQuestionsResDTO
import com.naposystems.pepito.dto.recoveryOlderAccountQuestions.sendAnswers.RecoveryOlderAccountQuestionsAnswers422DTO
import com.naposystems.pepito.dto.recoveryOlderAccountQuestions.sendAnswers.RecoveryOlderAccountQuestionsAnswersReqDTO
import com.naposystems.pepito.dto.recoveryOlderAccountQuestions.sendAnswers.RecoveryOlderAccountQuestionsAnswersResDTO
import com.naposystems.pepito.ui.recoveryOlderAccountQuestions.IContractRecoveryOlderAccountQuestions
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.utility.WebServiceUtils
import com.naposystems.pepito.webService.NapoleonApi
import com.squareup.moshi.Moshi
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject

class RecoveryOlderAccountQuestionsRepository @Inject constructor(
    private val napoleonApi: NapoleonApi,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val userLocalDataSource: UserLocalDataSource
) : IContractRecoveryOlderAccountQuestions.Repository {

    private lateinit var firebaseId: String

    private val moshi by lazy {
        Moshi.Builder().build()
    }

    override suspend fun getOlderQuestions(nickname: String): Response<RecoveryOlderAccountQuestionsResDTO> {
        return napoleonApi.getRecoveryOlderQuestions(nickname)
    }

    override suspend fun sendAnswers(
        nickname: String, answerOne: String, answerTwo: String
    ): Response<RecoveryOlderAccountQuestionsAnswersResDTO> {

        firebaseId = sharedPreferencesManager.getString(
            Constants.SharedPreferences.PREF_FIREBASE_ID,
            ""
        )

        val recoveryOlderAccountQuestionsAnswersReqDTO =
            RecoveryOlderAccountQuestionsAnswersReqDTO(
                nickname,
                firebaseId,
                answerOne,
                answerTwo
            )

        return napoleonApi.sendAnswersOldAccount(recoveryOlderAccountQuestionsAnswersReqDTO)
    }

    override suspend fun insertUser(recoveryOlderAccountDTO: RecoveryOlderAccountDTO) {
        val user = RecoveryOlderAccountDTO.toUserModel(recoveryOlderAccountDTO, firebaseId)
        userLocalDataSource.insertUser(user)
        sharedPreferencesManager.putInt(Constants.SharedPreferences.PREF_USER_ID, user.id)
    }

    override fun setRecoveredAccountPref() {
        sharedPreferencesManager.putInt(
            Constants.SharedPreferences.PREF_ACCOUNT_STATUS,
            Constants.AccountStatus.ACCOUNT_RECOVERED.id
        )
    }

    override suspend fun setAttemptPref() {
        var actualAttempts = sharedPreferencesManager.getInt(
            Constants.SharedPreferences.PREF_ACCOUNT_RECOVERY_ATTEMPTS
        )
        actualAttempts++

        sharedPreferencesManager.putInt(
            Constants.SharedPreferences.PREF_ACCOUNT_RECOVERY_ATTEMPTS, actualAttempts
        )
    }

    override suspend fun get422Error(response: ResponseBody): ArrayList<String> {
        val adapter = moshi.adapter(RecoveryOlderAccountQuestionsAnswers422DTO::class.java)
        val error = adapter.fromJson(response.string())

        return WebServiceUtils.get422Errors(error!!)
    }

    override suspend fun getDefaultQuestionsError(response: Response<RecoveryOlderAccountQuestionsResDTO>): ArrayList<String> {

        val adapter = moshi.adapter(RecoveryOlderAccountQuestionsErrorDTO::class.java)

        val updateUserInfoError = adapter.fromJson(response.errorBody()!!.string())

        return arrayListOf(updateUserInfoError!!.error)
    }

    override suspend fun getDefaultAnswersError(response: Response<RecoveryOlderAccountQuestionsAnswersResDTO>): ArrayList<String> {

        val adapter = moshi.adapter(RecoveryOlderAccountQuestionsErrorDTO::class.java)

        val updateUserInfoError = adapter.fromJson(response.errorBody()!!.string())

        return arrayListOf(updateUserInfoError!!.error)
    }

    override fun saveSecretKey(secretKey: String) {

        val crypto = Crypto()

        sharedPreferencesManager.putString(
            Constants.SharedPreferences.PREF_SECRET_KEY,
            crypto.decryptCipherTextWithRandomIV(secretKey, BuildConfig.KEY_OF_KEYS)
        )
    }
}