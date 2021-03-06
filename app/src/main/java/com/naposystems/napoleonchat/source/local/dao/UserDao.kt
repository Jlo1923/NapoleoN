package com.naposystems.napoleonchat.source.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.naposystems.napoleonchat.source.local.DBConstants
import com.naposystems.napoleonchat.source.local.entity.UserEntity

@Dao
interface UserDao {

    @Query(
        "SELECT * " +
                "FROM ${DBConstants.User.TABLE_NAME_USER} " +
                "ORDER BY ${DBConstants.User.COLUMN_CREATED_AT} ASC " +
                "LIMIT 1"
    )
    fun getMyUser(): UserEntity

    @Query(
        "SELECT * " +
                "FROM ${DBConstants.User.TABLE_NAME_USER} " +
                "WHERE ${DBConstants.User.COLUMN_FIREBASE_ID} = :firebaseId"
    )
    fun getUser(firebaseId: String): UserEntity

    @Query(
        "SELECT * " +
                "FROM ${DBConstants.User.TABLE_NAME_USER} " +
                "WHERE ${DBConstants.User.COLUMN_FIREBASE_ID} = :firebaseId"
    )
    fun getUserLiveData(firebaseId: String): LiveData<UserEntity>

    @Insert
    suspend fun insertUser(userEntity: UserEntity): Long

    @Update
    suspend fun updateUser(userEntity: UserEntity)

    @Query(
        "UPDATE ${DBConstants.User.TABLE_NAME_USER} " +
                "SET ${DBConstants.User.COLUMN_ACCESS_PIN} = :newAccessPin " +
                "WHERE ${DBConstants.User.COLUMN_FIREBASE_ID} = :firebaseId"
    )
    suspend fun updateAccessPin(newAccessPin: String, firebaseId: String)

    @Query(
        "UPDATE ${DBConstants.User.TABLE_NAME_USER} " +
                "SET ${DBConstants.User.COLUMN_CHAT_BACKGROUND} = :newBackground " +
                "WHERE ${DBConstants.User.COLUMN_FIREBASE_ID} = :firebaseId"
    )
    suspend fun updateChatBackground(newBackground: String, firebaseId: String)

    @Query(
        "UPDATE ${DBConstants.User.TABLE_NAME_USER} " +
                "SET ${DBConstants.User.COLUMN_STATUS} = :newStatus " +
                "WHERE ${DBConstants.User.COLUMN_FIREBASE_ID} = :firebaseId"
    )
    suspend fun updateStatus(newStatus: String, firebaseId: String)
}