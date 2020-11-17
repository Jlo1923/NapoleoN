package com.naposystems.napoleonchat.ui.profile

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.entity.User

interface IContractProfile {

    interface ViewModel {
        fun getLocalUser()
        fun updateLocalUser(newUser: User)
        fun getUser(): User?
        fun disconnectSocket()
    }

    interface Repository {
        suspend fun getUser(): LiveData<User>
        suspend fun updateLocalUser(user: User)
        fun disconnectSocket()
    }
}