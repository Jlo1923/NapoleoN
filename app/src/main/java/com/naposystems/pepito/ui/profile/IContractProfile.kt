package com.naposystems.pepito.ui.profile

import androidx.lifecycle.LiveData
import com.naposystems.pepito.entity.User

interface IContractProfile {

    interface ViewModel {
        fun getLocalUser()
        fun updateLocalUser(newUser: User)
        fun getUser(): User?
    }

    interface Repository {
        suspend fun getUser(): LiveData<User>
        suspend fun updateLocalUser(user: User)
    }
}