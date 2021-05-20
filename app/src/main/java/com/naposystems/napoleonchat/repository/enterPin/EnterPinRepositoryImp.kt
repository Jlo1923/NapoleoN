package com.naposystems.napoleonchat.repository.enterPin

import com.naposystems.napoleonchat.source.local.datasource.user.UserLocalDataSourceImp
import com.naposystems.napoleonchat.source.local.entity.UserEntity
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import javax.inject.Inject

class EnterPinRepositoryImp @Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val userLocalDataSourceImp: UserLocalDataSourceImp
) : EnterPinRepository {

    override suspend fun getAccessPin(): UserEntity {
        return userLocalDataSourceImp.getMyUser()
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

    override fun setBiometricPreference(option: Int) {
        sharedPreferencesManager.putInt(
            Constants.SharedPreferences.PREF_BIOMETRICS_OPTION,
            option
        )
    }
}