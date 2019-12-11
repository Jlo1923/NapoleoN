package com.naposystems.pepito.db.dao.user

import com.naposystems.pepito.entity.User

interface UserDatasource {

    suspend fun insertUser(user: User)

    suspend fun getUser(firebaseId: String): User

    suspend fun updateUser(user: User)

    suspend fun updateAccessPin(newAccessPin: String, firebaseId: String)
}