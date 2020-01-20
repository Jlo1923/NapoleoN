package com.naposystems.pepito.ui.splash

import com.naposystems.pepito.entity.User

interface IContractSplash {
    interface ViewModel {
        fun getUser()
    }

    interface Repository {
        suspend fun getUser(): User
    }
}

