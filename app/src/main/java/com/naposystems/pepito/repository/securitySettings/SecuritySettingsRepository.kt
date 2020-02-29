package com.naposystems.pepito.repository.securitySettings

import com.naposystems.pepito.ui.securitySettings.IContractSecuritySettings
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.SharedPreferencesManager
import javax.inject.Inject

class SecuritySettingsRepository @Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager
) : IContractSecuritySettings.Repository {

    override fun getSelfDestructTime(): Int {
        return sharedPreferencesManager.getInt(Constants.SharedPreferences.PREF_SELF_DESTRUCT_TIME)
    }
    override fun getAllowDownload(): Int {
        return sharedPreferencesManager.getInt(
            Constants.SharedPreferences.PREF_ALLOW_DOWNLOAD_ATTACHMENTS
        )
    }

    override fun updateAllowDownload(state: Int) {
        sharedPreferencesManager.putInt(
            Constants.SharedPreferences.PREF_ALLOW_DOWNLOAD_ATTACHMENTS,
            state
        )
    }

    override fun getMessageSelfDestructTimeNotSent(): Int {
        return sharedPreferencesManager.getInt(
            Constants.SharedPreferences.PREF_MESSAGE_SELF_DESTRUCT_TIME_NOT_SENT
        )
    }

    override fun getBiometricsOption(): Int {
        return sharedPreferencesManager.getInt(
            Constants.SharedPreferences.PREF_BIOMETRICS_OPTION
        )
    }

    override fun getTimeRequestAccessPin(): Int {
        return sharedPreferencesManager.getInt(Constants.SharedPreferences.PREF_TIME_REQUEST_ACCESS_PIN)
    }
}