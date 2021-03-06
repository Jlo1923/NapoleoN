package com.naposystems.napoleonchat.utility.sharedViewModels.userProfile

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.source.remote.dto.profile.UpdateUserInfoResDTO
import com.naposystems.napoleonchat.source.local.entity.UserEntity
import retrofit2.Response

interface IContractUserProfileShare {

    interface ViewModel {
        fun getUser()
        fun updateUserInfo(userEntity : UserEntity, updateUserInfoReqDTO: Any)
        fun updateUserLocal(userEntity: UserEntity)
    }

    interface Repository {
        suspend fun getUser() : LiveData<UserEntity>
        suspend fun updateUserInfo(updateUserInfoReqDTO: Any): Response<UpdateUserInfoResDTO>
        suspend fun updateUserLocal(userEntity: UserEntity)
    }

}