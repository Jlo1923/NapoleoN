package com.naposystems.napoleonchat.repository.splash

import com.naposystems.napoleonchat.db.dao.user.UserLocalDataSource
import com.naposystems.napoleonchat.entity.User
import com.naposystems.napoleonchat.ui.splash.IContractSplash
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import java.util.*
import javax.inject.Inject

class SplashRepository @Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val userDatasource: UserLocalDataSource
) : IContractSplash.Repository {

    override suspend fun getUser(): User {
//        val firebaseId = sharedPreferencesManager.getString(
//            Constants.SharedPreferences.PREF_FIREBASE_ID, ""
//        )
        return userDatasource.getMyUser()
    }

    override suspend fun getTimeRequestAccessPin(): Int {
        return sharedPreferencesManager.getInt(
            Constants.SharedPreferences.PREF_TIME_REQUEST_ACCESS_PIN
        )
    }

    override suspend fun getLockTime(): Long {
        return sharedPreferencesManager.getLong(
            Constants.SharedPreferences.PREF_LOCK_TIME_APP
        )
    }

    override suspend fun getLockType(): Int {
        return sharedPreferencesManager.getInt(
            Constants.SharedPreferences.PREF_TYPE_LOCK_APP
        )
    }

    override suspend fun getUnlockTimeApp(): Long {
        return sharedPreferencesManager.getLong(
            Constants.SharedPreferences.PREF_UNLOCK_TIME_APP
        )
    }

    override suspend fun getLockStatus(): Int {
        return sharedPreferencesManager.getInt(
            Constants.SharedPreferences.PREF_LOCK_STATUS
        )
    }

    override suspend fun getAccountStatus(): Int {
        return sharedPreferencesManager.getInt(
            Constants.SharedPreferences.PREF_ACCOUNT_STATUS
        )
    }



    override suspend fun setDefaultLanguage(language: String) {
        sharedPreferencesManager.putString(
            Constants.SharedPreferences.PREF_LANGUAGE_SELECTED, language
        )
    }



    override suspend fun setDefaultBiometricsOption(biometricOption: Int) {
        defaultPreferencesPutInt(
            Constants.SharedPreferences.PREF_BIOMETRICS_OPTION,
            biometricOption
        )
    }

    private fun defaultPreferencesPutInt(preference: String, data: Int) {
        val default = sharedPreferencesManager.getInt(
            preference
        )
        if (default == 0) {
            sharedPreferencesManager.putInt(
                preference,
                data
            )
        }
    }
}