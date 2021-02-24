package com.naposystems.napoleonchat.repository.sendCode

import com.naposystems.napoleonchat.dto.sendCode.SendCode422DTO
import com.naposystems.napoleonchat.dto.sendCode.SendCodeErrorDTO
import com.naposystems.napoleonchat.dto.sendCode.SendCodeReqDTO
import com.naposystems.napoleonchat.dto.sendCode.SendCodeResDTO
import com.naposystems.napoleonchat.ui.register.sendCode.IContractSendCode
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.utility.WebServiceUtils
import com.naposystems.napoleonchat.webService.NapoleonApi
import com.squareup.moshi.Moshi
import retrofit2.Response
import javax.inject.Inject

class SendCodeRepository @Inject constructor(
    private val napoleonApi: NapoleonApi,
    private val sharedPreferencesManager: SharedPreferencesManager
) : IContractSendCode.Repository {

    private val moshi by lazy {
        Moshi.Builder().build()
    }

    override suspend fun requestCode(): Response<SendCodeResDTO> {
        val firebase = sharedPreferencesManager.getString(
            Constants.SharedPreferences.PREF_FIREBASE_ID, ""
        )

        return napoleonApi.generateCode(SendCodeReqDTO(firebase))
    }

    override suspend fun setFirebaseId(newToken: String) {
        sharedPreferencesManager.putString(Constants.SharedPreferences.PREF_FIREBASE_ID, newToken)
    }

    override fun getTimeForNewCode(): Long {
        return sharedPreferencesManager.getLong(
            Constants.SharedPreferences.PREF_TIME_FOR_NEW_CODE
        )
    }

    override fun getTimeForEnterCode(): Long {
        return sharedPreferencesManager.getLong(
            Constants.SharedPreferences.PREF_TIME_FOR_RETRY_CODE
        )
    }

    override fun setAttemptNewCode() {
        val attempts = sharedPreferencesManager.getInt(
            Constants.SharedPreferences.PREF_ATTEMPTS_FOR_NEW_CODE
        )
        sharedPreferencesManager.putInt(
            Constants.SharedPreferences.PREF_ATTEMPTS_FOR_NEW_CODE, attempts.inc()
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

    override fun getAttemptsNewCode(): Int {
        return sharedPreferencesManager.getInt(
            Constants.SharedPreferences.PREF_ATTEMPTS_FOR_NEW_CODE
        )
    }

    override fun getAttemptsEnterCode(): Int {
        return sharedPreferencesManager.getInt(
            Constants.SharedPreferences.PREF_ATTEMPTS_FOR_RETRY_CODE
        )
    }

    override fun get422Error(response: Response<SendCodeResDTO>): ArrayList<String> {
        val adapter = moshi.adapter(SendCode422DTO::class.java)
        val error = adapter.fromJson(response.errorBody()!!.string())

        return WebServiceUtils.get422Errors(error!!)
    }

    override fun getDefaultError(response: Response<SendCodeResDTO>): ArrayList<String> {
        val adapter = moshi.adapter(SendCodeErrorDTO::class.java)
        val updateInfoError = adapter.fromJson(response.errorBody()!!.string())

        return arrayListOf(updateInfoError!!.error)
    }
}