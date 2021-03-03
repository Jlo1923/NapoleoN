package com.naposystems.napoleonchat.repository.recoveryAccountQuestions

import com.naposystems.napoleonchat.BuildConfig
import com.naposystems.napoleonchat.crypto.Crypto
import com.naposystems.napoleonchat.db.dao.user.UserLocalDataSource
import com.naposystems.napoleonchat.dto.recoveryAccountQuestions.*
import com.naposystems.napoleonchat.ui.recoveryAccountQuestions.IContractRecoveryAccountQuestions
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.utility.WebServiceUtils
import com.naposystems.napoleonchat.webService.NapoleonApi
import com.squareup.moshi.Moshi
import okhttp3.ResponseBody
import retrofit2.Response
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.collections.ArrayList

class RecoveryAccountQuestionsRepository @Inject constructor(
    private val napoleonApi: NapoleonApi,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val userLocalDataSource: UserLocalDataSource
) : IContractRecoveryAccountQuestions.Repository {

    private val moshi: Moshi by lazy {
        Moshi.Builder().build()
    }

    private lateinit var firebaseId: String

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

        Timber.d("AccountStatus setRecoveredAccountPref ${Constants.AccountStatus.ACCOUNT_RECOVERED.id}")

        sharedPreferencesManager.putInt(
            Constants.SharedPreferences.PREF_ACCOUNT_STATUS,
            Constants.AccountStatus.ACCOUNT_RECOVERED.id
        )
    }

    override fun saveSecretKey(secretKey: String) {

        val crypto = Crypto()

        val secretKey = crypto.decryptCipherTextWithRandomIV(secretKey, BuildConfig.KEY_OF_KEYS)

        sharedPreferencesManager.putString(
            Constants.SharedPreferences.PREF_SECRET_KEY,
            secretKey
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

    override suspend fun setFreeTrialPref(subscription: Boolean) {
        if (subscription) {
            sharedPreferencesManager.putLong(
                Constants.SharedPreferences.PREF_FREE_TRIAL, 0
            )
        } else {
            val firebaseId = sharedPreferencesManager.getString(
                Constants.SharedPreferences.PREF_FIREBASE_ID, ""
            )
            val createAtMilliseconds = TimeUnit.SECONDS.toMillis(
                userLocalDataSource.getMyUser().createAt
            )

            val calendar = Calendar.getInstance()
            calendar.time = Date(createAtMilliseconds)
            calendar.add(Calendar.DAY_OF_YEAR, Constants.FreeTrialUsers.THIRTY_DAYS.time)

            sharedPreferencesManager.putLong(
                Constants.SharedPreferences.PREF_FREE_TRIAL, calendar.timeInMillis
            )
        }
    }

    override fun getUnprocessableEntityError(response: ResponseBody): ArrayList<String> {
        val adapter = moshi.adapter(RecoveryAccountQuestionsUnprocessableEntityDTO::class.java)
        val error = adapter.fromJson(response.string())

        return WebServiceUtils.getUnprocessableEntityErrors(error!!)
    }

    override fun getError(response: ResponseBody): ArrayList<String> {

        val adapter = moshi.adapter(RecoveryAccountQuestionsErrorDTO::class.java)
        val error = adapter.fromJson(response.string())
        val errorList = ArrayList<String>()
        errorList.add(error!!.error)

        return errorList
    }

    override suspend fun insertUser(recoveryAccountUserDTO: RecoveryAccountUserDTO) {
        val user = RecoveryAccountUserDTO.toUserModel(recoveryAccountUserDTO, firebaseId)
        userLocalDataSource.insertUser(user)
//        sharedPreferencesManager.putInt(Constants.SharedPreferences.PREF_USER_ID, user.id)
    }
}