package com.naposystems.napoleonchat.repository.unlockAppTime

import com.naposystems.napoleonchat.ui.unlockAppTime.IContractUnlockAppTime
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import javax.inject.Inject

class UnlockAppTimeRepository @Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager
    ): IContractUnlockAppTime.Repository {


    override suspend fun getUnlockTime(): Long {
        return sharedPreferencesManager.getLong(
            Constants.SharedPreferences.PREF_UNLOCK_TIME_APP
        )
    }

    override suspend fun setAttempts(attempts: Int) {
        sharedPreferencesManager.putInt(
            Constants.SharedPreferences.PREF_UNLOCK_ATTEMPTS, attempts
        )
    }

    override suspend fun setLockType(type: Int) {
        sharedPreferencesManager.putInt(
            Constants.SharedPreferences.PREF_TYPE_LOCK_APP, type
        )
    }
}