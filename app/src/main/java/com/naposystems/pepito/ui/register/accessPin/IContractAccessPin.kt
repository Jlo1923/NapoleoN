package com.naposystems.pepito.ui.register.accessPin

import com.naposystems.pepito.dto.accessPin.CreateAccountReqDTO
import com.naposystems.pepito.dto.accessPin.CreateAccountResDTO
import com.naposystems.pepito.entity.User
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