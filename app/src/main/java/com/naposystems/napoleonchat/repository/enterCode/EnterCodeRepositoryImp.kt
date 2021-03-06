package com.naposystems.napoleonchat.repository.enterCode

import com.naposystems.napoleonchat.source.remote.api.NapoleonApi
import com.naposystems.napoleonchat.source.remote.dto.enterCode.EnterCodeErrorDTO
import com.naposystems.napoleonchat.source.remote.dto.enterCode.EnterCodeReqDTO
import com.naposystems.napoleonchat.source.remote.dto.enterCode.EnterCodeResDTO
import com.naposystems.napoleonchat.source.remote.dto.enterCode.EnterCodeUnprocessableEntityDTO
import com.naposystems.napoleonchat.source.remote.dto.sendCode.SendCodeErrorDTO
import com.naposystems.napoleonchat.source.remote.dto.sendCode.SendCodeReqDTO
import com.naposystems.napoleonchat.source.remote.dto.sendCode.SendCodeResDTO
import com.naposystems.napoleonchat.source.remote.dto.sendCode.SendCodeUnprocessableEntityDTO
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.utility.WebServiceUtils
import com.squareup.moshi.Moshi
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

class EnterCodeRepositoryImp
@Inject constructor(
    private val napoleonApi: NapoleonApi,
    private val sharedPreferencesManager: SharedPreferencesManager
) : EnterCodeRepository {

    private val moshi by lazy {
        Moshi.Builder().build()
    }

    override suspend fun sendCodeToWs(code: String): Response<EnterCodeResDTO> {
        val firebase = sharedPreferencesManager.getString(
            Constants.SharedPreferences.PREF_FIREBASE_ID, ""
        )
        return napoleonApi.verificateCode(
            EnterCodeReqDTO(firebase, code)
        )
    }

    override suspend fun codeForwarding(): Response<SendCodeResDTO> {
        val firebaseId = sharedPreferencesManager
            .getString(Constants.SharedPreferences.PREF_FIREBASE_ID, "")
        return napoleonApi.generateCode(SendCodeReqDTO(firebaseId))
    }

    override fun getAttemptsForNewCode(): Int {
        return sharedPreferencesManager.getInt(
            Constants.SharedPreferences.PREF_ATTEMPTS_FOR_NEW_CODE
        )
    }

    override fun setAttemptsForNewCode(attempts: Int) {
        sharedPreferencesManager.putInt(
            Constants.SharedPreferences.PREF_ATTEMPTS_FOR_NEW_CODE, attempts
        )
    }

    override fun setTimeForNewCode(time: Long) {
        sharedPreferencesManager.putLong(
            Constants.SharedPreferences.PREF_TIME_FOR_NEW_CODE, time
        )
    }

    override fun getAttemptsForRetryCode(): Int {
        return sharedPreferencesManager.getInt(
            Constants.SharedPreferences.PREF_ATTEMPTS_FOR_RETRY_CODE
        )
    }

    override fun getTimeForNewCode(): Long {
        return sharedPreferencesManager.getLong(
            Constants.SharedPreferences.PREF_TIME_FOR_NEW_CODE
        )
    }

    override fun setAttemptsForRetryCode(attempts: Int) {
        sharedPreferencesManager.putInt(
            Constants.SharedPreferences.PREF_ATTEMPTS_FOR_RETRY_CODE, attempts
        )
    }

    override fun setTimeForRetryCode(time: Long) {
        sharedPreferencesManager.putLong(
            Constants.SharedPreferences.PREF_TIME_FOR_RETRY_CODE, time
        )
    }

    override fun resetAttemptsEnterCode() {
        sharedPreferencesManager.putInt(
            Constants.SharedPreferences.PREF_ATTEMPTS_FOR_RETRY_CODE, 0
        )
    }

    override fun resetAttemptsNewCode() {
        sharedPreferencesManager.putInt(
            Constants.SharedPreferences.PREF_ATTEMPTS_FOR_NEW_CODE, 0
        )
    }

    override fun getUnprocessableEntityError(response: Response<EnterCodeResDTO>): ArrayList<String> {
        val adapter = moshi.adapter(EnterCodeUnprocessableEntityDTO::class.java)
        val error = adapter.fromJson(response.errorBody()!!.string())

        return WebServiceUtils.getUnprocessableEntityErrors(error!!)
    }

    override fun getDefaultError(response: Response<EnterCodeResDTO>): ArrayList<String> {
        val adapter = moshi.adapter(EnterCodeErrorDTO::class.java)
        val updateInfoError = adapter.fromJson(response.errorBody()!!.string())

        return arrayListOf(updateInfoError!!.error)
    }

    override fun getUnprocessableEntityErrorSendCode(response: Response<SendCodeResDTO>): ArrayList<String> {
        val adapter = moshi.adapter(SendCodeUnprocessableEntityDTO::class.java)
        val error = adapter.fromJson(response.errorBody()!!.string())

        return WebServiceUtils.getUnprocessableEntityErrors(error!!)
    }

    override fun getDefaultErrorSendCode(response: Response<SendCodeResDTO>): ArrayList<String> {
        val adapter = moshi.adapter(SendCodeErrorDTO::class.java)
        val updateInfoError = adapter.fromJson(response.errorBody()!!.string())

        return arrayListOf(updateInfoError!!.error)
    }

    override fun saveAccountStatus(id: Int) {

        Timber.d("AccountStatus saveAccountStatus $id")

        sharedPreferencesManager.putInt(
            Constants.SharedPreferences.PREF_ACCOUNT_STATUS,
            id
        )
    }
}