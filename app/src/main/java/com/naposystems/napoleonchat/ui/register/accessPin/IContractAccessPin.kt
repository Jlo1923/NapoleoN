package com.naposystems.napoleonchat.ui.register.accessPin

import com.naposystems.napoleonchat.source.remote.dto.accessPin.CreateAccountReqDTO
import com.naposystems.napoleonchat.source.remote.dto.accessPin.CreateAccountResDTO
import com.naposystems.napoleonchat.source.local.entity.UserEntity
import retrofit2.Response

interface IContractAccessPin {

    interface ViewModel {
        fun getFirebaseId(): String
        fun getLanguage(): String
        fun createAccount(createAccountReqDTO: CreateAccountReqDTO)
        fun createUser(userEntity: UserEntity)
        fun updateAccessPin(newAccessPin: String, firebaseId: String)
        fun createdUserPref()
        fun setFreeTrialPref(subscription: Boolean)
    }

    interface Repository {
        fun getFirebaseId(): String
        fun getLanguage(): String
        suspend fun createAccount(createAccountReqDTO: CreateAccountReqDTO): Response<CreateAccountResDTO>
        suspend fun createUser(userEntity: UserEntity)
        suspend fun updateAccessPin(newAccessPin: String, firebaseId: String)
        fun createdUserPref()
        suspend fun setFreeTrialPref(subscription: Boolean)
        fun saveSecretKey(secretKey: String)
    }
}