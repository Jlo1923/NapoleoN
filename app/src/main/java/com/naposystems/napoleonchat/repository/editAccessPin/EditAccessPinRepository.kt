package com.naposystems.napoleonchat.repository.editAccessPin

import com.naposystems.napoleonchat.db.dao.user.UserDataSource
import com.naposystems.napoleonchat.entity.User
import com.naposystems.napoleonchat.ui.editAccessPin.IContractEditAccessPin
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import javax.inject.Inject

class EditAccessPinRepository @Inject constructor(
    private val userLocalDataSource: UserDataSource,
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