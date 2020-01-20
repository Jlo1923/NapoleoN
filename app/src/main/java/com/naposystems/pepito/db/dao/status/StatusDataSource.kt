package com.naposystems.pepito.db.dao.status

import com.naposystems.pepito.entity.Status

interface StatusDataSource {

    suspend fun getStatus(): List<Status>

    suspend fun updateStatus(newStatus: String, firebaseId: String)
}