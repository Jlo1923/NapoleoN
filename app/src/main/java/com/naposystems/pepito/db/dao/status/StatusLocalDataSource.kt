package com.naposystems.pepito.db.dao.status

import com.naposystems.pepito.entity.Status
import javax.inject.Inject

class StatusLocalDataSource @Inject constructor(private val statusDao: StatusDao) :
    StatusDataSource {

    override suspend fun getStatus(): List<Status> {
        return statusDao.getStatus()
    }

    override suspend fun updateStatus(newStatus: String, firebaseId: String) {
        statusDao.updateStatus(newStatus, firebaseId)
    }
}