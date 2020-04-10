package com.naposystems.pepito.ui.status

import androidx.lifecycle.LiveData
import com.naposystems.pepito.dto.profile.UpdateUserInfoResDTO
import com.naposystems.pepito.dto.status.UserStatusReqDTO
import com.naposystems.pepito.entity.Status
import retrofit2.Response

interface IContractStatus {

    interface ViewModel {
        fun getStatus()
        fun updateStatus(textStatus: String)
        fun deleteStatus(status : Status)
    }

    interface Repository {
        suspend fun getStatus(): LiveData<List<Status>>
        suspend fun updateRemoteStatus(userStatus: UserStatusReqDTO): Response<UpdateUserInfoResDTO>
        suspend fun updateLocalStatus(newStatus: String, firebaseId: String)
        suspend fun insertNewStatus(listStatus: List<Status>)
        suspend fun deleteStatus(status : Status)
    }
}