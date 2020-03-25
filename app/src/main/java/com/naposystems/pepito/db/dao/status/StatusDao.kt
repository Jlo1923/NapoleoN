package com.naposystems.pepito.db.dao.status

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.naposystems.pepito.entity.Status

@Dao
interface StatusDao {

    @Insert
    fun insertStatus(status: List<Status>)

    @Query("SELECT * FROM status")
    fun getStatus(): LiveData<List<Status>>

    @Delete
    fun deleteStatus(status: Status)
}