package com.naposystems.napoleonchat.db.dao.user

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.db.NapoleonRoomDatabase
import com.naposystems.napoleonchat.entity.User
import javax.inject.Inject

class UserLocalDataSource @Inject constructor(
    private val napoleonDatabase: NapoleonRoomDatabase,
    private val userDao: UserDao) : UserDataSource {

    override suspend fun insertUser(user: User) {
        userDao.insertUser(user)
    }

    override suspend fun getUser(firebaseId: String): User {
        return userDao.getUser(firebaseId)
    }

    override suspend fun getUserLiveData(firebaseId: String): LiveData<User> {
        return userDao.getUserLiveData(firebaseId)
    }

    override suspend fun updateUser(user: User) {
        return userDao.updateUser(user)
    }

    override suspend fun updateAccessPin(newAccessPin: String, firebaseId: String) {
        return userDao.updateAccessPin(newAccessPin, firebaseId)
    }

    override suspend fun updateChatBackground(newBackground: String, firebaseId: String) {
        userDao.updateChatBackground(newBackground, firebaseId)
    }

    override suspend fun updateStatus(newStatus: String, firebaseId: String) {
        userDao.updateStatus(newStatus, firebaseId)
    }

    override suspend fun clearAllData() {
        napoleonDatabase.clearAllTables()
    }
}