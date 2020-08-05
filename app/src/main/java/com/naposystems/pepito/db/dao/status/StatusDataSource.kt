package com.naposystems.pepito.db.dao.status

import androidx.lifecycle.LiveData
import com.naposystems.pepito.entity.Status

interface StatusDataSource {

    suspend fun insertNewStatus(listStatus: List<Status>)

    suspend fun getStatus(): LiveData<MutableList<Status>>

    suspend fun deleteStatus(status: Status)
}