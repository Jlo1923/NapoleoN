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

    override suspend fun getUser(firebaseId: String): User {
        return userLocalDataSource.getUser(firebaseId)
    }

    override suspend fun getTheme(): Int {
        return sharedPreferencesManager.getInt(Constants.SharedPreferences.PREF_COLOR_SCHEME)
    }
}