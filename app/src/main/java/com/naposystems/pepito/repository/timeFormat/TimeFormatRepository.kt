package com.naposystems.pepito.repository.timeFormat

import com.naposystems.pepito.ui.timeFormat.IContractTimeFormat
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.SharedPreferencesManager
import javax.inject.Inject

class TimeFormatRepository@Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager
) : IContractTimeFormat.Repository {

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