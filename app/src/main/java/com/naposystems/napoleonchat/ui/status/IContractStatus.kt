package com.naposystems.napoleonchat.ui.status

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.dto.profile.UpdateUserInfoResDTO
import com.naposystems.napoleonchat.dto.status.UserStatusReqDTO
import com.naposystems.napoleonchat.entity.Status
import retrofit2.Response

interface IContractStatus {

    interface ViewModel {
        fun getStatus()
        fun updateStatus(textStatus: String)
        fun insertStatus(listStatus: List<Status>)
        fun deleteStatus(status : Status)
    }

    interface Repository {
        suspend fun getStatus(): LiveData<MutableList<Status>>
        suspend fun updateRemoteStatus(userStatus: UserStatusReqDTO): Response<UpdateUserInfoResDTO>
        suspend fun updateLocalStatus(newStatus: String, firebaseId: String)
        suspend fun insertNewStatus(listStatus: List<Status>)
        suspend fun deleteStatus(status : Status)
    }
}