package com.naposystems.napoleonchat.dialog.userDisplayFormat

import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import javax.inject.Inject

class UserDisplayFormatDialogRepositoryImp @Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager
) : UserDisplayFormatDialogRepository {

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