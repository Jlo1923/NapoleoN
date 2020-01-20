package com.naposystems.pepito.repository.appearanceSettings

import com.naposystems.pepito.db.dao.user.UserLocalDataSource
import com.naposystems.pepito.ui.appearanceSettings.IContractAppearanceSettings
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.SharedPreferencesManager
import javax.inject.Inject

class AppearanceSettingsRepository @Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val userLocalDataSource: UserLocalDataSource
) :
    IContractAppearanceSettings.Repository {

    override fun getColorScheme(): Int {
        return sharedPreferencesManager.getInt(Constants.SharedPreferences.PREF_COLOR_SCHEME)
    }

    override fun getUserDisplayFormat(): Int {
        return sharedPreferencesManager.getInt(Constants.SharedPreferences.PREF_USER_DISPLAY_FORMAT)
    }

    override suspend fun updateChatBackground(newBackground: String) {
        val firebaseId = sharedPreferencesManager
            .getString(Constants.SharedPreferences.PREF_FIREBASE_ID, "")
        userLocalDataSource.updateChatBackground(newBackground, firebaseId)
    }
}