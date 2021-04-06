package com.naposystems.napoleonchat.source.local.datasource.status

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.source.local.entity.StatusEntity
import com.naposystems.napoleonchat.source.local.dao.StatusDao
import javax.inject.Inject

class StatusLocalDataSourceImp @Inject constructor(private val statusDao: StatusDao) :
    StatusLocalDataSource {

    override suspend fun insertNewStatus(listStatusEntities: List<StatusEntity>) {
        statusDao.insertStatus(listStatusEntities)
    }

    override suspend fun getStatus(): LiveData<MutableList<StatusEntity>> {
        return statusDao.getStatus()
    }

    override suspend fun deleteStatus(statusEntity: StatusEntity) {
        statusDao.deleteStatus(statusEntity)
    }
}