package com.naposystems.pepito.ui.profile

import com.naposystems.pepito.dto.profile.UpdateUserInfoReqDTO
import com.naposystems.pepito.dto.profile.UpdateUserInfoResDTO
import com.naposystems.pepito.entity.User
import retrofit2.Response

interface IContractProfile {

    interface ViewModel {
        fun getUser()
        fun updateUserInfo(updateUserInfoReqDTO: UpdateUserInfoReqDTO)
        fun updateLocalUser(newUser: User)
    }

    interface Repository {
        suspend fun getUser(firebaseId: String): User
        suspend fun updateUserInfo(updateUserInfoReqDTO: UpdateUserInfoReqDTO): Response<UpdateUserInfoResDTO>
        suspend fun updateLocalUser(user: User)
    }
}