package com.naposystems.napoleonchat.ui.register.accessPin

import com.naposystems.napoleonchat.dto.accessPin.CreateAccountReqDTO
import com.naposystems.napoleonchat.dto.accessPin.CreateAccountResDTO
import com.naposystems.napoleonchat.entity.User
import retrofit2.Response

interface IContractAccessPin {

    interface ViewModel {
        fun getFirebaseId(): String
        fun getLanguage(): String
        fun createAccount(createAccountReqDTO: CreateAccountReqDTO)
        fun createUser(user: User)
        fun updateAccessPin(newAccessPin: String, firebaseId: String)
        fun createdUserPref()
        fun insertFreeTrialPref()
    }

    interface Repository {
        fun getFirebaseId(): String
        fun getLanguage(): String
        suspend fun createAccount(createAccountReqDTO: CreateAccountReqDTO): Response<CreateAccountResDTO>
        suspend fun createUser(user: User)
        suspend fun updateAccessPin(newAccessPin: String, firebaseId: String)
        fun createdUserPref()
        suspend fun setFreeTrialPref()
        fun saveSecretKey(secretKey: String)
    }
}