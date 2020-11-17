package com.naposystems.napoleonchat.repository.registerRecoveryAccount

import com.naposystems.napoleonchat.ui.registerRecoveryAccount.IContractRegisterRecoveryAccount
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import javax.inject.Inject

class RegisterRecoveryAccountRepository @Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager
) : IContractRegisterRecoveryAccount.Repository {

    override fun getRecoveryQuestionsPref(): Int {
        return sharedPreferencesManager.getInt(Constants.SharedPreferences.PREF_RECOVERY_QUESTIONS_SAVED)
    }
}