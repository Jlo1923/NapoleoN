package com.naposystems.napoleonchat.repository.colorScheme

import com.naposystems.napoleonchat.ui.colorScheme.IContractColorScheme
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import javax.inject.Inject

class ColorSchemeRepository @Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager
) : IContractColorScheme.Repository {

    override fun getActualTheme(): Int {
        return sharedPreferencesManager.getInt(Constants.SharedPreferences.PREF_COLOR_SCHEME)
    }

    override suspend fun saveTheme(newTheme: Int) {
        sharedPreferencesManager.putInt(Constants.SharedPreferences.PREF_COLOR_SCHEME, newTheme)
    }
}