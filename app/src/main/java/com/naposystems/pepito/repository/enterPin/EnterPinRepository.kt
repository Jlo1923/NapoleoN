package com.naposystems.pepito.repository.enterPin

import com.naposystems.pepito.db.dao.user.UserLocalDataSource
import com.naposystems.pepito.entity.User
import com.naposystems.pepito.ui.enterPin.IContractEnterPin
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.SharedPreferencesManager
import javax.inject.Inject

class EnterPinRepository @Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val userLocalDataSource: UserLocalDataSource
): IContractEnterPin.Repository {
    override suspend fun getAccessPin(): User {
        val firebaseId = sharedPreferencesManager.getString(
            Constants.SharedPreferences.PREF_FIREBASE_ID,
            ""
        )
        return userLocalDataSource.getUser(firebaseId)
    }

    override suspend fun getAttempts(): Int {
        return sharedPreferencesManager.getInt(
            Constants.SharedPreferences.PREF_UNLOCK_ATTEMPTS
        )
    }

    override suspend fun setAttempts(attempts: Int) {
        sharedPreferencesManager.putInt(
            Constants.SharedPreferences.PREF_UNLOCK_ATTEMPTS, attempts
        )
    }

    override suspend fun getTotalAttempts(): Int {
        return sharedPreferencesManager.getInt(
            Constants.SharedPreferences.PREF_UNLOCK_TOTAL_ATTEMPTS
        )
    }

    override suspend fun setTotalAttempts(attempts: Int) {
        sharedPreferencesManager.putInt(
            Constants.SharedPreferences.PREF_UNLOCK_TOTAL_ATTEMPTS, attempts
        )
    }

    override suspend fun setUnlockAppTime(time: Long) {
        sharedPreferencesManager.putLong(
            Constants.SharedPreferences.PREF_UNLOCK_TIME_APP, time
        )
    }

    override suspend fun setLockType(type: Int) {
        sharedPreferencesManager.putInt(
            Constants.SharedPreferences.PREF_TYPE_LOCK_APP, type
        )
    }

    override suspend fun setLockStatus(state: Int) {
        sharedPreferencesManager.putInt(
            Constants.SharedPreferences.PREF_LOCK_STATUS, state
        )
    }

    override suspend fun getBiometricsOption(): Int {
        return sharedPreferencesManager.getInt(
            Constants.SharedPreferences.PREF_BIOMETRICS_OPTION
        )
    }
}