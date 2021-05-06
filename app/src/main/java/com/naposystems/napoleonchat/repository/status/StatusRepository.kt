package com.naposystems.napoleonchat.repository.status

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.source.local.entity.StatusEntity
import com.naposystems.napoleonchat.source.remote.dto.profile.UpdateUserInfoResDTO
import com.naposystems.napoleonchat.source.remote.dto.status.UserStatusReqDTO
import retrofit2.Response

interface StatusRepository {
    suspend fun getStatus(): LiveData<MutableList<StatusEntity>>
    suspend fun updateRemoteStatus(userStatus: UserStatusReqDTO): Response<UpdateUserInfoResDTO>
    suspend fun updateLocalStatus(newStatus: String, firebaseId: String)
    suspend fun insertNewStatus(listStatusEntities: List<StatusEntity>)
    suspend fun deleteStatus(statusEntity: StatusEntity)
    fun getUnprocessableEntityError(response: Response<UpdateUserInfoResDTO>): ArrayList<String>
    fun getDefaultError(response: Response<UpdateUserInfoResDTO>): ArrayList<String>
}