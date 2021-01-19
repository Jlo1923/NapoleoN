package com.naposystems.napoleonchat.repository.defaultPreferences

import android.content.Context
import android.content.res.Configuration
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.utility.sharedViewModels.defaulPreferences.IContractDefaultPreferences
import javax.inject.Inject

class DefaultPreferencesRepository @Inject constructor(
    private val context: Context,
    private val sharedPreferencesManager: SharedPreferencesManager
) : IContractDefaultPreferences.Repository {

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

    override suspend fun setDefaultLockType() {
        defaultPreferencesPutInt(
            Constants.SharedPreferences.PREF_TYPE_LOCK_APP,
            Constants.LockTypeApp.LOCK_FOR_TIME_REQUEST_PIN.type
        )
    }

    override suspend fun setDefaultSelfDestructTimeMessageNotSent() {
        defaultPreferencesPutInt(
            Constants.SharedPreferences.PREF_MESSAGE_SELF_DESTRUCT_TIME_NOT_SENT,
            Constants.SelfDestructTime.EVERY_SEVEN_DAYS_ERROR.time
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

    override suspend fun setDefaultNotificationMessageChannelId() {
        defaultPreferencesPutInt(
            Constants.SharedPreferences.PREF_NOTIFICATION_MESSAGE_CHANNEL_ID,
            0
        )
    }

    override suspend fun setDefaultNotificationGroupChannelId() {
        defaultPreferencesPutInt(
            Constants.SharedPreferences.PREF_NOTIFICATION_GROUP_CHANNEL_ID,
            0
        )
    }

    override suspend fun setDefaultDialogSubscription() {
        defaultPreferencesPutInt(Constants.SharedPreferences.PREF_DIALOG_SUBSCRIPTION, 0)
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