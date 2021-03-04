package com.naposystems.napoleonchat.ui.profile

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.source.local.entity.UserEntity

interface IContractProfile {

    interface ViewModel {
        fun getLocalUser()
        fun updateLocalUser(newUserEntity: UserEntity)
        fun getUser(): UserEntity?
        fun disconnectSocket()
    }

    interface Repository {
        suspend fun getUser(): LiveData<UserEntity>
        suspend fun updateLocalUser(userEntity: UserEntity)
        fun disconnectSocket()
    }
}