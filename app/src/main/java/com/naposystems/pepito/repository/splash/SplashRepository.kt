package com.naposystems.pepito.repository.splash

import com.naposystems.pepito.db.dao.user.UserLocalDataSource
import com.naposystems.pepito.entity.User
import com.naposystems.pepito.ui.splash.IContractSplash
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.SharedPreferencesManager
import javax.inject.Inject

class SplashRepository @Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val userDatasource: UserLocalDataSource
) : IContractSplash.Repository {

    override suspend fun getUser(): User {
        val firebaseId = sharedPreferencesManager.getString(
            Constants.SharedPreferences.PREF_FIREBASE_ID, ""
        )

        return userDatasource.getUser(firebaseId)
    }
}