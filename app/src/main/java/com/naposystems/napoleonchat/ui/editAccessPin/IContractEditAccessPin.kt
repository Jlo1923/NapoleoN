package com.naposystems.napoleonchat.ui.editAccessPin

import com.naposystems.napoleonchat.source.local.entity.UserEntity

interface IContractEditAccessPin {

    interface ViewModel {
        fun getLocalUser()
        fun validateAccessPin(newAccessPin: String): Boolean
        fun updateAccessPin(newAccessPin: String)
    }

    interface Repository {
        suspend fun getLocalUser(): UserEntity
        suspend fun updateAccessPin(newAccessPin: String, firebaseId: String)
    }
}