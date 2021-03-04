package com.naposystems.napoleonchat.repository.appearanceSettings

import com.naposystems.napoleonchat.source.local.datasource.user.UserLocalDataSourceImp
import com.naposystems.napoleonchat.ui.appearanceSettings.IContractAppearanceSettings
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import javax.inject.Inject

class AppearanceSettingsRepository @Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val userLocalDataSourceImp: UserLocalDataSourceImp
) :
    IContractAppearanceSettings.Repository {

    override fun getColorScheme(): Int {
        return sharedPreferencesManager.getInt(Constants.SharedPreferences.PREF_COLOR_SCHEME)
    }

    override fun getUserDisplayFormat(): Int {
        return sharedPreferencesManager.getInt(Constants.SharedPreferences.PREF_USER_DISPLAY_FORMAT)
    }

    override fun getTimeFormat(): Int {
        return sharedPreferencesManager.getInt(Constants.SharedPreferences.PREF_TIME_FORMAT)
    }

    override suspend fun getConversationBackground(): String {
//        val firebaseId = sharedPreferencesManager
//            .getString(Constants.SharedPreferences.PREF_FIREBASE_ID, "")

        val user = userLocalDataSourceImp.getMyUser()

        return user.chatBackground
    }
}