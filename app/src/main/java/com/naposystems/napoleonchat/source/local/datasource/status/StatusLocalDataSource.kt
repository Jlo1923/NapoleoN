package com.naposystems.napoleonchat.source.local.datasource.status

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.source.local.entity.StatusEntity

interface StatusLocalDataSource {

    suspend fun insertNewStatus(listStatusEntities: List<StatusEntity>)

    suspend fun getStatus(): LiveData<MutableList<StatusEntity>>

    suspend fun deleteStatus(statusEntity: StatusEntity)
}