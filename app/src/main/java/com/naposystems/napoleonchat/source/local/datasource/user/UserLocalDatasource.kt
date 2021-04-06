package com.naposystems.napoleonchat.source.local.datasource.user

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.source.local.entity.UserEntity

interface UserLocalDataSource {

    fun getMyUser():UserEntity

    suspend fun insertUser(userEntity: UserEntity)

    suspend fun getUser(firebaseId: String): UserEntity

    suspend fun getUserLiveData(firebaseId: String): LiveData<UserEntity>

    suspend fun updateUser(userEntity: UserEntity)

    suspend fun updateAccessPin(newAccessPin: String, firebaseId: String)

    suspend fun updateChatBackground(newBackground: String, firebaseId: String)

    suspend fun updateStatus(newStatus: String, firebaseId: String)

    suspend fun clearAllData()
}