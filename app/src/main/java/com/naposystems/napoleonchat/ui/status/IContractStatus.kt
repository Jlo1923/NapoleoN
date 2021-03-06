package com.naposystems.napoleonchat.ui.status

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.source.remote.dto.profile.UpdateUserInfoResDTO
import com.naposystems.napoleonchat.source.remote.dto.status.UserStatusReqDTO
import com.naposystems.napoleonchat.source.local.entity.StatusEntity
import retrofit2.Response

interface IContractStatus {

    interface ViewModel {
        fun getStatus()
        fun updateStatus(textStatus: String)
        fun insertStatus(listStatusEntities: List<StatusEntity>)
        fun deleteStatus(statusEntity : StatusEntity)
    }

    interface Repository {
        suspend fun getStatus(): LiveData<MutableList<StatusEntity>>
        suspend fun updateRemoteStatus(userStatus: UserStatusReqDTO): Response<UpdateUserInfoResDTO>
        suspend fun updateLocalStatus(newStatus: String, firebaseId: String)
        suspend fun insertNewStatus(listStatusEntities: List<StatusEntity>)
        suspend fun deleteStatus(statusEntity : StatusEntity)
    }
}