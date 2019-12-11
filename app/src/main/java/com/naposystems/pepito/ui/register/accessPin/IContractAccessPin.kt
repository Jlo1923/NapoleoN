package com.naposystems.pepito.ui.register.accessPin

import com.naposystems.pepito.dto.accessPin.CreateAccountReqDTO
import com.naposystems.pepito.dto.accessPin.CreateAccountResDTO
import com.naposystems.pepito.entity.User
import retrofit2.Response

interface IContractAccessPin {

    interface ViewModel {
        fun createAccount(createAccountReqDTO: CreateAccountReqDTO)
        fun createUser(user: User)
    }

    interface Repository {
        suspend fun createAccount(createAccountReqDTO: CreateAccountReqDTO): Response<CreateAccountResDTO>
        suspend fun createUser(user: User)
    }
}