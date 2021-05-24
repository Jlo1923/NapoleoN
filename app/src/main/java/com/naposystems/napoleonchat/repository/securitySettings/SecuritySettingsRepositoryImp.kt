package com.naposystems.napoleonchat.repository.securitySettings

import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import javax.inject.Inject

class SecuritySettingsRepositoryImp @Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager
) : SecuritySettingsRepository {

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

    override fun getBiometricsOption(): Int {
        return sharedPreferencesManager.getInt(
            Constants.SharedPreferences.PREF_BIOMETRICS_OPTION
        )
    }

    override fun getTimeRequestAccessPin(): Int {
        return sharedPreferencesManager.getInt(Constants.SharedPreferences.PREF_TIME_REQUEST_ACCESS_PIN)
    }
}