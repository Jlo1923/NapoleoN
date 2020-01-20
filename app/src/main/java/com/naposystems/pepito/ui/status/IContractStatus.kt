package com.naposystems.pepito.ui.status

import com.naposystems.pepito.dto.profile.UpdateUserInfoReqDTO
import com.naposystems.pepito.dto.profile.UpdateUserInfoResDTO
import com.naposystems.pepito.entity.Status
import retrofit2.Response

interface IContractStatus {

    interface ViewModel {
        fun getStatus()
        fun updateStatus(updateUserInfoReqDTO: UpdateUserInfoReqDTO)
    }

    interface Repository {
        suspend fun getStatus(): List<Status>
        suspend fun updateRemoteStatus(updateUserInfoReqDTO: UpdateUserInfoReqDTO): Response<UpdateUserInfoResDTO>
        suspend fun updateLocalStatus(newStatus: String, firebaseId: String)
    }
}