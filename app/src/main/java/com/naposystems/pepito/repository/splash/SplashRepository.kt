package com.naposystems.pepito.repository.splash

import com.naposystems.pepito.db.dao.user.UserLocalDataSource
import com.naposystems.pepito.entity.User
import com.naposystems.pepito.ui.splash.IContractSplash
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.SharedPreferencesManager
import javax.inject.Inject

class SplashRepository @Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val userDatasource: UserLocalDataSource
) : IContractSplash.Repository {

    override suspend fun getUser(): User {
        val firebaseId = sharedPreferencesManager.getString(
            Constants.SharedPreferences.PREF_FIREBASE_ID, ""
        )
        return userDatasource.getUser(firebaseId)
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

    override suspend fun setDefaultTheme() {
        val default = sharedPreferencesManager.getInt(
            Constants.SharedPreferences.PREF_COLOR_SCHEME
        )
        if (default == 0) {
            sharedPreferencesManager.putInt(
                Constants.SharedPreferences.PREF_COLOR_SCHEME,
                Constants.ColorScheme.LIGHT_THEME.scheme
            )
        }
    }

    override suspend fun setDefaultLanguage(language: String) {
        sharedPreferencesManager.putString(
            Constants.SharedPreferences.PREF_LANGUAGE_SELECTED, language
        )
    }

    override suspend fun setDefaultUserDisplayFormat() {
        val default = sharedPreferencesManager.getInt(
            Constants.SharedPreferences.PREF_USER_DISPLAY_FORMAT
        )
        if (default == 0) {
            sharedPreferencesManager.putInt(
                Constants.SharedPreferences.PREF_USER_DISPLAY_FORMAT,
                Constants.UserDisplayFormat.NAME_AND_NICKNAME.format
            )
        }
    }

    override suspend fun setDefaultSelfDestructTime() {
        val default = sharedPreferencesManager.getInt(
            Constants.SharedPreferences.PREF_SELF_DESTRUCT_TIME
        )
        if (default == 0) {
            sharedPreferencesManager.putInt(
                Constants.SharedPreferences.PREF_SELF_DESTRUCT_TIME,
                Constants.SelfDestructTime.EVERY_TWENTY_FOUR_HOURS.time
            )
        }
    }

    override suspend fun setDefaultTimeRequestAccessPin() {
        val default = sharedPreferencesManager.getInt(
            Constants.SharedPreferences.PREF_TIME_REQUEST_ACCESS_PIN
        )
        if (default == 0) {
            sharedPreferencesManager.putInt(
                Constants.SharedPreferences.PREF_TIME_REQUEST_ACCESS_PIN,
                Constants.TimeRequestAccessPin.THIRTY_SECONDS.time
            )
        }
    }

    override suspend fun setDefaultAllowDownloadAttachments() {
        val default = sharedPreferencesManager.getInt(
            Constants.SharedPreferences.PREF_ALLOW_DOWNLOAD_ATTACHMENTS
        )
        if (default == 0) {
            sharedPreferencesManager.putInt(
                Constants.SharedPreferences.PREF_ALLOW_DOWNLOAD_ATTACHMENTS,
                Constants.AllowDownloadAttachments.YES.option
            )
        }
    }

    override suspend fun setDefaultBiometricsOption(biometricOption: Int) {
        sharedPreferencesManager.putInt(
            Constants.SharedPreferences.PREF_BIOMETRICS_OPTION, biometricOption
        )
    }

    override suspend fun setDefaultLockType() {
        val default = sharedPreferencesManager.getInt(
            Constants.SharedPreferences.PREF_TYPE_LOCK_APP
        )
        if (default == 0) {
            sharedPreferencesManager.putInt(
                Constants.SharedPreferences.PREF_TYPE_LOCK_APP,
                Constants.LockTypeApp.LOCK_FOR_TIME_REQUEST_PIN.type
            )
        }
    }


}