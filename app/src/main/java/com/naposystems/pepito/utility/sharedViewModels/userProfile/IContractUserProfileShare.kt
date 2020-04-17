package com.naposystems.pepito.utility.sharedViewModels.userProfile

import androidx.lifecycle.LiveData
import com.naposystems.pepito.dto.profile.UpdateUserInfoResDTO
import com.naposystems.pepito.entity.User
import retrofit2.Response

interface IContractUserProfileShare {

    interface ViewModel {
        fun getUser()
        fun updateUserInfo(user : User, updateUserInfoReqDTO: Any)
        fun updateUserLocal(user: User)
    }

    interface Repository {
        suspend fun getUser() : LiveData<User>
        suspend fun updateUserInfo(updateUserInfoReqDTO: Any): Response<UpdateUserInfoResDTO>
        suspend fun updateUserLocal(user: User)
    }

}