package com.naposystems.pepito.repository.timeAccessPin

import com.naposystems.pepito.ui.timeAccessPin.IContractTimeAccessPin
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.Constants.SharedPreferences.PREF_TIME_REQUEST_ACCESS_PIN
import com.naposystems.pepito.utility.SharedPreferencesManager
import javax.inject.Inject

class TimeAccessPinRepository @Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager
) : IContractTimeAccessPin.Repository {

    override suspend fun getTimeAccessPin(): Int {
        return sharedPreferencesManager.getInt(PREF_TIME_REQUEST_ACCESS_PIN)
    }

    override suspend fun setTimeAccessPin(time: Int) {
        sharedPreferencesManager.putInt(PREF_TIME_REQUEST_ACCESS_PIN, time)
    }

    override suspend fun setLockType(type: Int) {
        sharedPreferencesManager.putInt(
            Constants.SharedPreferences.PREF_TYPE_LOCK_APP, type
        )
    }
}