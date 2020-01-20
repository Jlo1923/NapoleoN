package com.naposystems.pepito.repository.registerRecoveryAccount

import com.naposystems.pepito.ui.registerRecoveryAccount.IContractRegisterRecoveryAccount
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.SharedPreferencesManager
import javax.inject.Inject

class RegisterRecoveryAccountRepository @Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager
) : IContractRegisterRecoveryAccount.Repository {

    override fun getRecoveryQuestionsPref(): Int {
        return sharedPreferencesManager.getInt(Constants.SharedPreferences.PREF_RECOVERY_QUESTIONS_SAVED)
    }
}