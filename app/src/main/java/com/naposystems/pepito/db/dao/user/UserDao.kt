package com.naposystems.pepito.db.dao.user

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.naposystems.pepito.entity.User

@Dao
interface UserDao {

    @Query("SELECT * FROM user WHERE firebase_id=:firebaseId")
    fun getUser(firebaseId: String): User

    @Insert
    suspend fun insertUser(user: User)
}