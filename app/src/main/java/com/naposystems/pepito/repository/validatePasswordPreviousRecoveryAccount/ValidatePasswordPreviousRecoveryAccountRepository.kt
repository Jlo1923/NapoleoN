package com.naposystems.pepito.repository.validatePasswordPreviousRecoveryAccount

import com.naposystems.pepito.dto.validatePasswordPreviousRecoveryAccount.ValidatePasswordPreviousRecoveryAccount422DTO
import com.naposystems.pepito.dto.validatePasswordPreviousRecoveryAccount.ValidatePasswordPreviousRecoveryAccountErrorDTO
import com.naposystems.pepito.dto.validatePasswordPreviousRecoveryAccount.ValidatePasswordPreviousRecoveryAccountReqDTO
import com.naposystems.pepito.dto.validatePasswordPreviousRecoveryAccount.ValidatePasswordPreviousRecoveryAccountResDTO
import com.naposystems.pepito.ui.validatePasswordPreviousRecoveryAccount.IContractValidatePasswordPreviousRecoveryAccount
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.utility.WebServiceUtils
import com.naposystems.pepito.webService.NapoleonApi
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