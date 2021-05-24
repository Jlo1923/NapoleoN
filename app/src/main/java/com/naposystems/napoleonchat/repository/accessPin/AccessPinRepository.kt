package com.naposystems.napoleonchat.repository.accessPin

import com.naposystems.napoleonchat.source.local.entity.UserEntity
import com.naposystems.napoleonchat.source.remote.dto.accessPin.CreateAccountReqDTO
import com.naposystems.napoleonchat.source.remote.dto.accessPin.CreateAccountResDTO
import okhttp3.ResponseBody
import retrofit2.Response

interface AccessPinRepository {
    fun getFirebaseId(): String
    fun getLanguage(): String
    suspend fun createAccount(createAccountReqDTO: CreateAccountReqDTO): Response<CreateAccountResDTO>
    suspend fun createUser(userEntity: UserEntity)
    suspend fun updateAccessPin(newAccessPin: String, firebaseId: String)
    fun createdUserPref()
    suspend fun setFreeTrialPref(subscription: Boolean)
    fun saveSecretKey(secretKey: String)
    fun getUnprocessableEntityError(response: Response<CreateAccountResDTO>): ArrayList<String>
    fun getError(response: Response<CreateAccountResDTO>): ArrayList<String>
}