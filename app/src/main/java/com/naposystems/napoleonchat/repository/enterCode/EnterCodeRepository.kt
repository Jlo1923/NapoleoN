package com.naposystems.napoleonchat.repository.enterCode

import com.naposystems.napoleonchat.dto.enterCode.EnterCode422DTO
import com.naposystems.napoleonchat.dto.enterCode.EnterCodeErrorDTO
import com.naposystems.napoleonchat.dto.enterCode.EnterCodeReqDTO
import com.naposystems.napoleonchat.dto.enterCode.EnterCodeResDTO
import com.naposystems.napoleonchat.dto.sendCode.SendCode422DTO
import com.naposystems.napoleonchat.dto.sendCode.SendCodeErrorDTO
import com.naposystems.napoleonchat.dto.sendCode.SendCodeReqDTO
import com.naposystems.napoleonchat.dto.sendCode.SendCodeResDTO
import com.naposystems.napoleonchat.ui.register.enterCode.IContractEnterCode
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.utility.WebServiceUtils
import com.naposystems.napoleonchat.webService.NapoleonApi
import com.squareup.moshi.Moshi
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

class EnterCodeRepository @Inject constructor(
    private val napoleonApi: NapoleonApi,
    private val sharedPreferencesManager: SharedPreferencesManager
) : IContractEnterCode.Repository {

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

    override fun get422Error(response: Response<EnterCodeResDTO>): ArrayList<String> {
        val adapter = moshi.adapter(EnterCode422DTO::class.java)
        val error = adapter.fromJson(response.errorBody()!!.string())

        return WebServiceUtils.get422Errors(error!!)
    }

    override fun getDefaultError(response: Response<EnterCodeResDTO>): ArrayList<String> {
        val adapter = moshi.adapter(EnterCodeErrorDTO::class.java)
        val updateInfoError = adapter.fromJson(response.errorBody()!!.string())

        return arrayListOf(updateInfoError!!.error)
    }

    override fun get422ErrorSendCode(response: Response<SendCodeResDTO>): ArrayList<String> {
        val adapter = moshi.adapter(SendCode422DTO::class.java)
        val error = adapter.fromJson(response.errorBody()!!.string())

        return WebServiceUtils.get422Errors(error!!)
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