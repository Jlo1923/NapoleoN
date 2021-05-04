package com.naposystems.napoleonchat.utility.sharedViewModels.userProfile

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.source.local.entity.UserEntity
import com.naposystems.napoleonchat.source.remote.dto.profile.UpdateUserInfoResDTO
import retrofit2.Response

interface UserProfileSharedRepository {
    suspend fun getUser(): LiveData<UserEntity>
    suspend fun updateUserInfo(updateUserInfoReqDTO: Any): Response<UpdateUserInfoResDTO>
    suspend fun updateUserLocal(userEntity: UserEntity)
    fun getUnprocessableEntityError(response: Response<UpdateUserInfoResDTO>): ArrayList<String>
    fun getDefaultError(response: Response<UpdateUserInfoResDTO>): ArrayList<String>
}