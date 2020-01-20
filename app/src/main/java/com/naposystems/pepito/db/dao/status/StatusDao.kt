package com.naposystems.pepito.db.dao.status

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.naposystems.pepito.entity.Status

@Dao
interface StatusDao {

    @Insert
    fun insertStatus(status: List<Status>)

    @Query("SELECT * FROM status")
    suspend fun getStatus(): List<Status>

    @Query("UPDATE user SET status=:newStatus WHERE firebase_id=:firebaseId")
    suspend fun updateStatus(newStatus: String, firebaseId: String)
}