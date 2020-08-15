package com.naposystems.napoleonchat.repository.splash

import android.content.Context
import android.content.res.Configuration
import com.naposystems.napoleonchat.db.dao.user.UserLocalDataSource
import com.naposystems.napoleonchat.entity.User
import com.naposystems.napoleonchat.ui.splash.IContractSplash
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import javax.inject.Inject

class SplashRepository @Inject constructor(
    private val context: Context,
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
        val nightModeFlags: Int = context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        val defaultTheme = when (nightModeFlags) {
            Configuration.UI_MODE_NIGHT_YES -> Constants.ColorScheme.DARK_THEME.scheme
            else -> Constants.ColorScheme.LIGHT_THEME.scheme
        }
        defaultPreferencesPutInt(
            Constants.SharedPreferences.PREF_COLOR_SCHEME,
            defaultTheme
        )
    }

    override suspend fun setDefaultLanguage(language: String) {
        sharedPreferencesManager.putString(
            Constants.SharedPreferences.PREF_LANGUAGE_SELECTED, language
        )
    }

    override suspend fun setDefaultUserDisplayFormat() {
        defaultPreferencesPutInt(
            Constants.SharedPreferences.PREF_USER_DISPLAY_FORMAT,
            Constants.UserDisplayFormat.NAME_AND_NICKNAME.format
        )
    }

    override suspend fun setDefaultTimeFormat() {
        defaultPreferencesPutInt(
            Constants.SharedPreferences.PREF_TIME_FORMAT,
            Constants.TimeFormat.EVERY_TWENTY_FOUR_HOURS.time
        )
    }

    override suspend fun setDefaultSelfDestructTime() {
        defaultPreferencesPutInt(
            Constants.SharedPreferences.PREF_SELF_DESTRUCT_TIME,
            Constants.SelfDestructTime.EVERY_TWELVE_HOURS.time
        )
    }

    override suspend fun setDefaultTimeRequestAccessPin() {
        defaultPreferencesPutInt(
            Constants.SharedPreferences.PREF_TIME_REQUEST_ACCESS_PIN,
            Constants.TimeRequestAccessPin.THIRTY_SECONDS.time
        )
    }

    override suspend fun setDefaultAllowDownloadAttachments() {
        defaultPreferencesPutInt(
            Constants.SharedPreferences.PREF_ALLOW_DOWNLOAD_ATTACHMENTS,
            Constants.AllowDownloadAttachments.YES.option
        )
    }

    override suspend fun setDefaultBiometricsOption(biometricOption: Int) {
        defaultPreferencesPutInt(
            Constants.SharedPreferences.PREF_BIOMETRICS_OPTION,
            biometricOption
        )
    }

    override suspend fun setDefaultLockType() {
        defaultPreferencesPutInt(
            Constants.SharedPreferences.PREF_TYPE_LOCK_APP,
            Constants.LockTypeApp.LOCK_FOR_TIME_REQUEST_PIN.type
        )
    }

    override suspend fun setDefaultSelfDestructTimeMessageNotSent() {
        defaultPreferencesPutInt(
            Constants.SharedPreferences.PREF_MESSAGE_SELF_DESTRUCT_TIME_NOT_SENT,
            Constants.MessageSelfDestructTimeNotSent.SEVEN_DAYS.time
        )
    }

    override suspend fun setDefaultAttemptsForRetryCode() {
        defaultPreferencesPutInt(Constants.SharedPreferences.PREF_ATTEMPTS_FOR_RETRY_CODE, 0)
    }

    override suspend fun setDefaultTimeForRetryCode() {
        defaultPreferencesPutLong(Constants.SharedPreferences.PREF_TIME_FOR_RETRY_CODE, 0)
    }

    override suspend fun setDefaultAttemptsForNewCode() {
        defaultPreferencesPutInt(Constants.SharedPreferences.PREF_ATTEMPTS_FOR_NEW_CODE, 0)
    }

    private fun defaultPreferencesPutLong(preference: String, data: Long) {
        val default = sharedPreferencesManager.getLong(
            preference
        )
        if (default == 0L) {
            sharedPreferencesManager.putLong(
                preference,
                data
            )
        }
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