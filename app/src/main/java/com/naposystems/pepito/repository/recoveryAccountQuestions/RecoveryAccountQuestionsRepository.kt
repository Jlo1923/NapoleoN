package com.naposystems.pepito.repository.recoveryAccountQuestions

import com.naposystems.pepito.BuildConfig
import com.naposystems.pepito.db.dao.user.UserLocalDataSource
import com.naposystems.pepito.dto.recoveryAccountQuestions.*
import com.naposystems.pepito.ui.recoveryAccountQuestions.IContractRecoveryAccountQuestions
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.crypto.Crypto
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.utility.WebServiceUtils
import com.naposystems.pepito.webService.NapoleonApi
import com.squareup.moshi.Moshi
import okhttp3.ResponseBody
import retrofit2.Response
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.collections.ArrayList

class RecoveryAccountQuestionsRepository @Inject constructor(
    private val napoleonApi: NapoleonApi,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val userLocalDataSource: UserLocalDataSource
) : IContractRecoveryAccountQuestions.Repository {

    private lateinit var firebaseId: String

    private val moshi: Moshi by lazy {
        Moshi.Builder().build()
    }

    override suspend fun sendRecoveryAnswers(
        nickname: String,
        answers: List<RecoveryAccountAnswersDTO>
    ): Response<RecoveryAccountQuestionsResDTO> {
        firebaseId =
            sharedPreferencesManager.getString(Constants.SharedPreferences.PREF_FIREBASE_ID, "")

        val request = RecoveryAccountQuestionsReqDTO(nickname, firebaseId, answers)

        return napoleonApi.sendAnswers(request)
    }

    override fun setRecoveredAccountPref() {
        sharedPreferencesManager.putInt(
            Constants.SharedPreferences.PREF_ACCOUNT_STATUS,
            Constants.AccountStatus.ACCOUNT_RECOVERED.id
        )
    }

    override fun saveSecretKey(secretKey: String) {
        val crypto = Crypto()

        sharedPreferencesManager.putString(
            Constants.SharedPreferences.PREF_SECRET_KEY,
            crypto.decryptCipherTextWithRandomIV(secretKey, BuildConfig.KEY_OF_KEYS)
        )
    }

    override fun setRecoveredQuestionsPref() {
        sharedPreferencesManager.putInt(
            Constants.SharedPreferences.PREF_RECOVERY_QUESTIONS_SAVED,
            Constants.RecoveryQuestionsSaved.SAVED_QUESTIONS.id
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

    override suspend fun setFreeTrialPref() {
        val firebaseId = sharedPreferencesManager.getString(
            Constants.SharedPreferences.PREF_FIREBASE_ID, ""
        )
        val createAtMiliseconds = TimeUnit.SECONDS.toMillis(
            userLocalDataSource.getUser(firebaseId).createAt
        )

        val calendar = Calendar.getInstance()
        calendar.time = Date(createAtMiliseconds)
        calendar.add(Calendar.DAY_OF_YEAR, Constants.FreeTrialUsers.FORTY_FIVE_DAYS.time)

        sharedPreferencesManager.putLong(
            Constants.SharedPreferences.PREF_FREE_TRIAL, calendar.timeInMillis
        )
    }

    override fun get422Error(response: ResponseBody): ArrayList<String> {
        val adapter = moshi.adapter(RecoveryAccountQuestions422DTO::class.java)
        val error = adapter.fromJson(response.string())

        return WebServiceUtils.get422Errors(error!!)
    }

    override fun getError(response: ResponseBody): ArrayList<String> {

        val moshi = Moshi.Builder().build()
        val adapter = moshi.adapter(RecoveryAccountQuestionsErrorDTO::class.java)
        val error = adapter.fromJson(response.string())
        val errorList = ArrayList<String>()
        errorList.add(error!!.error)

        return errorList
    }

    override suspend fun insertUser(recoveryAccountUserDTO: RecoveryAccountUserDTO) {
        val user = RecoveryAccountUserDTO.toUserModel(recoveryAccountUserDTO, firebaseId)
        userLocalDataSource.insertUser(user)
        sharedPreferencesManager.putInt(Constants.SharedPreferences.PREF_USER_ID, user.id)
    }
}