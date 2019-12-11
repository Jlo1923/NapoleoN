package com.naposystems.pepito.repository.editAccessPin

import com.naposystems.pepito.db.dao.user.UserDatasource
import com.naposystems.pepito.entity.User
import com.naposystems.pepito.ui.editAccessPin.IContractEditAccessPin
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.SharedPreferencesManager
import javax.inject.Inject

class EditAccessPinRepository @Inject constructor(
    private val userLocalDataSource: UserDatasource,
    private val sharedPreferencesManager: SharedPreferencesManager
) : IContractEditAccessPin.Repository {

    override suspend fun getLocalUser(): User {
        val firebaseId = sharedPreferencesManager.getString(
            Constants.SharedPreferences.PREF_FIREBASE_ID, ""
        )
        return userLocalDataSource.getUser(firebaseId)
    }

    override suspend fun updateAccessPin(newAccessPin: String, firebaseId: String) {
        userLocalDataSource.updateAccessPin(newAccessPin, firebaseId)
    }
}