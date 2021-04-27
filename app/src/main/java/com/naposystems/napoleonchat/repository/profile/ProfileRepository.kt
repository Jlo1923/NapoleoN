package com.naposystems.napoleonchat.repository.profile

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.source.local.entity.UserEntity

interface ProfileRepository {
    suspend fun getUser(): LiveData<UserEntity>
    suspend fun updateLocalUser(userEntity: UserEntity)
    fun disconnectSocket()
}