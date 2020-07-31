package com.naposystems.pepito.db.dao.user

import androidx.lifecycle.LiveData
import com.naposystems.pepito.entity.User

interface UserDataSource {

    suspend fun insertUser(user: User)

    suspend fun getUser(firebaseId: String): User

    suspend fun getUserLiveData(firebaseId: String): LiveData<User>

    suspend fun updateUser(user: User)

    suspend fun updateAccessPin(newAccessPin: String, firebaseId: String)

    suspend fun updateChatBackground(newBackground: String, firebaseId: String)

    suspend fun updateStatus(newStatus: String, firebaseId: String)

    suspend fun clearAllData()
}