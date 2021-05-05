package com.naposystems.napoleonchat.dialog.timeFormat

import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import javax.inject.Inject

class TimeFormatDialogRepositoryImp
@Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager
) : TimeFormatDialogRepository {

    override fun setTimeFormat(format: Int) {
        sharedPreferencesManager.putInt(
            Constants.SharedPreferences.PREF_TIME_FORMAT,
            format
        )
    }

    override fun getTimeFormat(): Int {
        return sharedPreferencesManager.getInt(Constants.SharedPreferences.PREF_TIME_FORMAT)
    }
}