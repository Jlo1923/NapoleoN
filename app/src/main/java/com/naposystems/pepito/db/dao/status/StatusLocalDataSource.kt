package com.naposystems.pepito.db.dao.status

import androidx.lifecycle.LiveData
import com.naposystems.pepito.entity.Status
import javax.inject.Inject

class StatusLocalDataSource @Inject constructor(private val statusDao: StatusDao) :
    StatusDataSource {

    override suspend fun insertNewStatus(listStatus: List<Status>) {
        statusDao.insertStatus(listStatus)
    }

    override suspend fun getStatus(): LiveData<List<Status>> {
        return statusDao.getStatus()
    }

    override suspend fun deleteStatus(status: Status) {
        statusDao.deleteStatus(status)
    }
}