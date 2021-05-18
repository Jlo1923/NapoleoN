package com.naposystems.napoleonchat.repository.editAccessPin

import com.naposystems.napoleonchat.source.local.datasource.user.UserLocalDataSource
import com.naposystems.napoleonchat.source.local.entity.UserEntity
import javax.inject.Inject

class EditAccessPinRepositoryImp @Inject constructor(
    private val userLocalDataSource: UserLocalDataSource
) : EditAccessPinRepository {

    override suspend fun getLocalUser(): UserEntity {
        return userLocalDataSource.getMyUser()
    }

    override suspend fun updateAccessPin(newAccessPin: String, firebaseId: String) {
        userLocalDataSource.updateAccessPin(newAccessPin, firebaseId)
    }
}