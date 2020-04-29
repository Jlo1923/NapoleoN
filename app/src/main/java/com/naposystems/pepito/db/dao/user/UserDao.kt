package com.naposystems.pepito.db.dao.user

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.naposystems.pepito.entity.User

@Dao
interface UserDao {

    @Query("SELECT * FROM user WHERE firebase_id=:firebaseId")
    fun getUser(firebaseId: String): User

    @Query("SELECT * FROM user WHERE firebase_id=:firebaseId")
    fun getUserLiveData(firebaseId: String): LiveData<User>

    @Insert
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)

    @Query("UPDATE user SET access_pin=:newAccessPin WHERE firebase_id=:firebaseId")
    suspend fun updateAccessPin(newAccessPin: String, firebaseId: String)

    @Query("UPDATE user SET chat_background=:newBackground WHERE firebase_id=:firebaseId")
    suspend fun updateChatBackground(newBackground: String, firebaseId: String)

    @Query("UPDATE user SET status=:newStatus WHERE firebase_id=:firebaseId")
    suspend fun updateStatus(newStatus: String, firebaseId: String)
}