package com.naposystems.napoleonchat.repository.editAccessPin

import com.naposystems.napoleonchat.source.local.entity.UserEntity

interface EditAccessPinRepository {
    suspend fun getLocalUser(): UserEntity
    suspend fun updateAccessPin(newAccessPin: String, firebaseId: String)
}