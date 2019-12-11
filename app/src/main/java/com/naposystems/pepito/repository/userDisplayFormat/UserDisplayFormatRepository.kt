package com.naposystems.pepito.repository.userDisplayFormat

import com.naposystems.pepito.ui.userDisplayFormat.IContractUserDisplayFormat
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.SharedPreferencesManager
import javax.inject.Inject

class UserDisplayFormatRepository @Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager
) : IContractUserDisplayFormat.Repository {

    override fun setUserDisplayFormat(format: Int) {
        sharedPreferencesManager.putInt(
            Constants.SharedPreferences.PREF_USER_DISPLAY_FORMAT,
            format
        )
    }

    override fun getUserDisplayFormat(): Int {
        return sharedPreferencesManager.getInt(Constants.SharedPreferences.PREF_USER_DISPLAY_FORMAT)
    }
}