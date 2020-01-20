package com.naposystems.pepito.ui.editAccessPin

import com.naposystems.pepito.entity.User

interface IContractEditAccessPin {

    interface ViewModel {
        fun getLocalUser()
        fun validateAccessPin(newAccessPin: String): Boolean
        fun updateAccessPin(newAccessPin: String)
    }

    interface Repository {
        suspend fun getLocalUser(): User
        suspend fun updateAccessPin(newAccessPin: String, firebaseId: String)
    }
}