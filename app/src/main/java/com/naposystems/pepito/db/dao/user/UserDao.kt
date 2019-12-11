package com.naposystems.pepito.db.dao.user

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.naposystems.pepito.entity.User

@Dao
interface UserDao {

    @Query("SELECT * FROM user WHERE firebase_id=:firebaseId")
    fun getUser(firebaseId: String): User

    @Insert
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Query("UPDATE user SET access_pin=:newAccessPin WHERE firebase_id=:firebaseId")
    suspend fun updateAccessPin(newAccessPin: String, firebaseId: String)
}