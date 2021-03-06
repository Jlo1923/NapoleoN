package com.naposystems.napoleonchat.repository.editAccessPin

import com.naposystems.napoleonchat.source.local.datasource.user.UserLocalDataSource
import com.naposystems.napoleonchat.source.local.entity.UserEntity
import com.naposystems.napoleonchat.ui.editAccessPin.IContractEditAccessPin
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import javax.inject.Inject

class EditAccessPinRepository @Inject constructor(
    private val userLocalDataSource: UserLocalDataSource,
    private val sharedPreferencesManager: SharedPreferencesManager
) : IContractEditAccessPin.Repository {

    override suspend fun getLocalUser(): UserEntity {
//        val firebaseId = sharedPreferencesManager.getString(
//            Constants.SharedPreferences.PREF_FIREBASE_ID, ""
//        )
        return userLocalDataSource.getMyUser()
    }

    override suspend fun updateAccessPin(newAccessPin: String, firebaseId: String) {
        userLocalDataSource.updateAccessPin(newAccessPin, firebaseId)
    }
}