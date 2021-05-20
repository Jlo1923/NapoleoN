package com.naposystems.napoleonchat.repository.splash

import android.content.Context
import com.naposystems.napoleonchat.source.local.datasource.user.UserLocalDataSource
import com.naposystems.napoleonchat.source.local.entity.UserEntity
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.FileManager
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SplashRepositoryImp @Inject constructor(
    private val context: Context,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val userLocalDataSource: UserLocalDataSource
) : SplashRepository {
    override suspend fun clearData() {
        sharedPreferencesManager.reset()
        userLocalDataSource.clearAllData()

        withContext(Dispatchers.IO) {
            FileManager.deleteAllFiles(context)
        }
    }

    override suspend fun getUser(): UserEntity {
        return userLocalDataSource.getMyUser()
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