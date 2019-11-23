package com.naposystems.pepito.repository.accessPin

import com.naposystems.pepito.db.dao.user.UserLocalDataSource
import com.naposystems.pepito.dto.accessPin.CreateAccount422DTO
import com.naposystems.pepito.dto.accessPin.CreateAccountErrorDTO
import com.naposystems.pepito.dto.accessPin.CreateAccountReqDTO
import com.naposystems.pepito.dto.accessPin.CreateAccountResDTO
import com.naposystems.pepito.entity.User
import com.naposystems.pepito.ui.register.accessPin.IContractAccessPin
import com.naposystems.pepito.webService.NapoleonApi
import com.squareup.moshi.Moshi
import retrofit2.Response
import javax.inject.Inject

class CreateAccountRepository @Inject constructor(
    private val userLocalDataSource: UserLocalDataSource,
    private val napoleonApi: NapoleonApi
) :
    IContractAccessPin.Repository {

    override suspend fun createAccount(createAccountReqDTO: CreateAccountReqDTO): Response<CreateAccountResDTO> {
        return napoleonApi.createAccount(createAccountReqDTO)
    }

    override suspend fun createUser(user: User) {
        userLocalDataSource.insertUser(user)
    }

    fun get422Error(response: Response<CreateAccountResDTO>): ArrayList<String> {
        val moshi = Moshi.Builder().build()

        val adapter = moshi.adapter(CreateAccount422DTO::class.java)

        val enterCodeError = adapter.fromJson(response.errorBody()!!.string())

        val errorList = ArrayList<String>()

        if (enterCodeError!!.nickname.isNotEmpty()) {
            for (error in enterCodeError.nickname) {
                errorList.add(error)
            }
        }

        if (enterCodeError.password.isNotEmpty()) {
            for (error in enterCodeError.password) {
                errorList.add(error)
            }
        }

        if (enterCodeError.firebaseId.isNotEmpty()) {
            for (error in enterCodeError.firebaseId) {
                errorList.add(error)
            }
        }

        return errorList
    }

    fun getError(response: Response<CreateAccountResDTO>): ArrayList<String> {
        val moshi = Moshi.Builder().build()

        val adapter = moshi.adapter(CreateAccountErrorDTO::class.java)

        val enterCodeError = adapter.fromJson(response.errorBody()!!.string())

        val errorList = ArrayList<String>()

        errorList.add(enterCodeError!!.error)

        return errorList
    }
}