package com.naposystems.pepito.repository.recoveryAccount

import com.naposystems.pepito.dto.recoveryAccount.RecoveryAccountErrorDTO
import com.naposystems.pepito.dto.recoveryAccount.RecoveryAccountResDTO
import com.naposystems.pepito.dto.recoveryAccount.RecoveryAccountUserTypeResDTO
import com.naposystems.pepito.ui.recoveryAccount.IContractRecoveryAccount
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.webService.NapoleonApi
import com.squareup.moshi.Moshi
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject

class RecoveryAccountRepository @Inject constructor(
    private val napoleonApi: NapoleonApi,
    private val sharedPreferencesManager: SharedPreferencesManager
) : IContractRecoveryAccount.Repository {

    override suspend fun getUserType(nickname: String): Response<RecoveryAccountUserTypeResDTO> {
        return napoleonApi.getRecoveryQuestions(nickname)
    }

    override suspend fun getRecoveryAttempts(): Int {
        return sharedPreferencesManager.getInt(
            Constants.SharedPreferences.PREF_ACCOUNT_RECOVERY_ATTEMPTS
        )
    }

    override fun getError(response: ResponseBody): ArrayList<String> {
        val moshi = Moshi.Builder().build()
        val adapter = moshi.adapter(RecoveryAccountErrorDTO::class.java)
        val errorJson = adapter.fromJson(response.string())
        val errorList = ArrayList<String>()
        
        errorList.add(errorJson!!.error)
        return errorList
    }
}