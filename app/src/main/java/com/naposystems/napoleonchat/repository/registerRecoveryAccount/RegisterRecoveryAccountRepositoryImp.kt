package com.naposystems.napoleonchat.repository.registerRecoveryAccount

import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import javax.inject.Inject

class RegisterRecoveryAccountRepositoryImp @Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager
) : RegisterRecoveryAccountRepository {

    override fun getRecoveryQuestionsPref(): Int {
        return sharedPreferencesManager.getInt(Constants.SharedPreferences.PREF_RECOVERY_QUESTIONS_SAVED)
    }
}