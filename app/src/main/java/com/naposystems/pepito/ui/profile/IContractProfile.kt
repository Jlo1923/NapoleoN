package com.naposystems.pepito.ui.profile

import androidx.lifecycle.LiveData
import com.naposystems.pepito.entity.User

interface IContractProfile {

    interface ViewModel {
        fun getUser()
        fun updateLocalUser(newUser: User)
    }

    interface Repository {
        suspend fun getUser(): LiveData<User>
        suspend fun updateLocalUser(user: User)
    }
}