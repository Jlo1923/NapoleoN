package com.naposystems.pepito.repository.mainActivity

import com.naposystems.pepito.db.dao.user.UserLocalDataSource
import com.naposystems.pepito.entity.User
import com.naposystems.pepito.ui.mainActivity.IContractMainActivity
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.SharedPreferencesManager
import javax.inject.Inject

class MainActivityRepository @Inject constructor(
    private val userLocalDataSource: UserLocalDataSource,
    private val sharedPreferencesManager: SharedPreferencesManager
) :
    IContractMainActivity.Repository {

    override suspend fun getUser(): User {
        val firebaseId = sharedPreferencesManager
            .getString(Constants.SharedPreferences.PREF_FIREBASE_ID, "")
        return userLocalDataSource.getUser(firebaseId)
    }

    override suspend fun getTheme(): Int {
        return sharedPreferencesManager.getInt(Constants.SharedPreferences.PREF_COLOR_SCHEME)
    }

    override suspend fun getAccountStatus(): Int {
        return sharedPreferencesManager.getInt(Constants.SharedPreferences.PREF_ACCOUNT_STATUS)
    }

    override fun getOutputControl(): Int {
        return sharedPreferencesManager.getInt(Constants.SharedPreferences.PREF_OUTPUT_CONTROL)
    }

    override suspend fun setOutputControl(state: Int) {
        sharedPreferencesManager.putInt(
            Constants.SharedPreferences.PREF_OUTPUT_CONTROL, state
        )
    }

    override suspend fun getTimeRequestAccessPin(): Int {
        return sharedPreferencesManager.getInt(
            Constants.SharedPreferences.PREF_TIME_REQUEST_ACCESS_PIN
        )
    }

    override fun setLockTimeApp(lockTime: Long) {
        sharedPreferencesManager.putLong(
            Constants.SharedPreferences.PREF_LOCK_TIME_APP, lockTime
        )
    }

    override suspend fun setLockStatus(state: Int) {
        sharedPreferencesManager.putInt(
            Constants.SharedPreferences.PREF_LOCK_STATUS, state
        )
    }

    override suspend fun getLockTimeApp(): Long {
        return sharedPreferencesManager.getLong(
            Constants.SharedPreferences.PREF_LOCK_TIME_APP
        )
    }
}