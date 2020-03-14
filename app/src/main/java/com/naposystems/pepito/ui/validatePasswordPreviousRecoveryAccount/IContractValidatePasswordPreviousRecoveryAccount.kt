package com.naposystems.pepito.ui.validatePasswordPreviousRecoveryAccount

import com.naposystems.pepito.dto.validatePasswordPreviousRecoveryAccount.ValidatePasswordPreviousRecoveryAccountResDTO
import retrofit2.Response

interface IContractValidatePasswordPreviousRecoveryAccount {
    interface ViewModel {
        fun sendPassword(nickname: String, password: String)
        fun setAttemptPref()
    }

    interface Repository {
        suspend fun sendPassword(nickname: String, password: String): Response<ValidatePasswordPreviousRecoveryAccountResDTO>
        suspend fun setAttemptPref()
        fun get422Error(response: Response<ValidatePasswordPreviousRecoveryAccountResDTO>): List<String>
        fun getDefaultError(response: Response<ValidatePasswordPreviousRecoveryAccountResDTO>): List<String>
    }
}