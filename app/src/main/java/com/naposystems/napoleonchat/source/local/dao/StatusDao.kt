package com.naposystems.napoleonchat.source.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.naposystems.napoleonchat.source.local.DBConstants
import com.naposystems.napoleonchat.source.local.entity.StatusEntity

@Dao
interface StatusDao {

    @Insert
    fun insertStatus(statusEntities: List<StatusEntity>)

    @Query(
        "SELECT * " +
                "FROM ${DBConstants.Status.TABLE_NAME_STATUS}"
    )
    fun getStatus(): LiveData<MutableList<StatusEntity>>

    @Delete
    fun deleteStatus(statusEntity: StatusEntity)
}