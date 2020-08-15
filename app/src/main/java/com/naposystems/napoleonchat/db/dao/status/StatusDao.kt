package com.naposystems.napoleonchat.db.dao.status

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.naposystems.napoleonchat.entity.Status

@Dao
interface StatusDao {

    @Insert
    fun insertStatus(status: List<Status>)

    @Query("SELECT * FROM status")
    fun getStatus(): LiveData<MutableList<Status>>

    @Delete
    fun deleteStatus(status: Status)
}