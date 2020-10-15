package com.naposystems.napoleonchat.repository.validatePasswordPreviousRecoveryAccount

import com.naposystems.napoleonchat.dto.validatePasswordPreviousRecoveryAccount.ValidatePasswordPreviousRecoveryAccount422DTO
import com.naposystems.napoleonchat.dto.validatePasswordPreviousRecoveryAccount.ValidatePasswordPreviousRecoveryAccountErrorDTO
import com.naposystems.napoleonchat.dto.validatePasswordPreviousRecoveryAccount.ValidatePasswordPreviousRecoveryAccountReqDTO
import com.naposystems.napoleonchat.dto.validatePasswordPreviousRecoveryAccount.ValidatePasswordPreviousRecoveryAccountResDTO
import com.naposystems.napoleonchat.ui.validatePasswordPreviousRecoveryAccount.IContractValidatePasswordPreviousRecoveryAccount
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.utility.WebServiceUtils
import com.naposystems.napoleonchat.webService.NapoleonApi
import com.squareup.moshi.Moshi
import retrofit2.Response
import javax.inject.Inject

class ValidatePasswordPreviousRecoveryAccountRepository @Inject constructor(
    private val napoleonApi: NapoleonApi,
    private val sharedPreferencesManager: SharedPreferencesManager
) : IContractValidatePasswordPreviousRecoveryAccount.Repository {

    private val moshi by lazy {
        Moshi.Builder().build()
    }

    override suspend fun sendPassword(
        nickname: String,
        password: String
    ): Response<ValidatePasswordPreviousRecoveryAccountResDTO> {

        val firebase = sharedPreferencesManager.getString(
            Constants.SharedPreferences.PREF_FIREBASE_ID, ""
        )
        val validatePasswordPreviousRecoveryAccountReqDTO =
            ValidatePasswordPreviousRecoveryAccountReqDTO(
                nickname,
                firebase,
                password
            )

        return napoleonApi.sendPasswordOlderAccount(validatePasswordPreviousRecoveryAccountReqDTO)
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

    override fun get422Error(response: Response<ValidatePasswordPreviousRecoveryAccountResDTO>): List<String> {
        val adapter = moshi.adapter(ValidatePasswordPreviousRecoveryAccount422DTO::class.java)

        val enterCodeError = adapter.fromJson(response.errorBody()!!.string())

        return WebServiceUtils.get422Errors(enterCodeError!!)
    }

    override fun getDefaultError(response: Response<ValidatePasswordPreviousRecoveryAccountResDTO>): ArrayList<String> {

        val adapter = moshi.adapter(ValidatePasswordPreviousRecoveryAccountErrorDTO::class.java)

        val updateUserInfoError = adapter.fromJson(response.errorBody()!!.string())

        return arrayListOf(updateUserInfoError!!.error)
    }
}