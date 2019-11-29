package com.naposystems.pepito.db.dao.user

import com.naposystems.pepito.entity.User
import javax.inject.Inject

class UserLocalDataSource @Inject constructor(private val userDao: UserDao) : UserDatasource {

    override suspend fun insertUser(user: User) {
        userDao.insertUser(user)
    }

    override suspend fun getUser(firebaseId: String): User {
        return userDao.getUser(firebaseId)
    }

    override suspend fun updateUser(user: User) {
        return userDao.updateUser(user)
    }
}