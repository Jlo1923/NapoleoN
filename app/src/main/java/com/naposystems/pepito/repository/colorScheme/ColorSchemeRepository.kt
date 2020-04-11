package com.naposystems.pepito.repository.colorScheme

import com.naposystems.pepito.ui.colorScheme.IContractColorScheme
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.SharedPreferencesManager
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