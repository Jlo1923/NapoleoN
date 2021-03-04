package com.naposystems.napoleonchat.source.local.datasource.user

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.source.local.db.NapoleonRoomDatabase
import com.naposystems.napoleonchat.source.local.entity.UserEntity
import com.naposystems.napoleonchat.source.local.dao.UserDao
import javax.inject.Inject

class UserLocalDataSourceImp @Inject constructor(
    private val napoleonDatabase: NapoleonRoomDatabase,
    private val userDao: UserDao
) : UserLocalDataSource {

    override fun getMyUser(): UserEntity {

        return userDao.getMyUser()
    }

    override suspend fun insertUser(userEntity: UserEntity) {
        userDao.insertUser(userEntity)
    }

    override suspend fun getUser(firebaseId: String): UserEntity {
        return userDao.getUser(firebaseId)
    }

    override suspend fun getUserLiveData(firebaseId: String): LiveData<UserEntity> {
        return userDao.getUserLiveData(firebaseId)
    }

    override suspend fun updateUser(userEntity: UserEntity) {
        return userDao.updateUser(userEntity)
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