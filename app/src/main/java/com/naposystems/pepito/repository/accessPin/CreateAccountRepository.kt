package com.naposystems.pepito.repository.accessPin

import com.naposystems.pepito.db.dao.user.UserLocalDataSource
import com.naposystems.pepito.dto.accessPin.CreateAccount422DTO
import com.naposystems.pepito.dto.accessPin.CreateAccountErrorDTO
import com.naposystems.pepito.dto.accessPin.CreateAccountReqDTO
import com.naposystems.pepito.dto.accessPin.CreateAccountResDTO
import com.naposystems.pepito.entity.User
import com.naposystems.pepito.ui.register.accessPin.IContractAccessPin
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.utility.WebServiceUtils
import com.naposystems.pepito.webService.NapoleonApi
import com.squareup.moshi.Moshi
import retrofit2.Response
import javax.inject.Inject

class CreateAccountRepository @Inject constructor(
    private val userLocalDataSource: UserLocalDataSource,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val napoleonApi: NapoleonApi
) :
    IContractAccessPin.Repository {

    override fun getFirebaseId(): String {
        return sharedPreferencesManager.getString(
            Constants.SharedPreferences.PREF_FIREBASE_ID,
            ""
        )
    }

    override fun getLanguage(): String {
        return sharedPreferencesManager.getString(
            Constants.SharedPreferences.PREF_LANGUAGE_SELECTED,
            ""
        )
    }

    override suspend fun createAccount(createAccountReqDTO: CreateAccountReqDTO): Response<CreateAccountResDTO> {
        return napoleonApi.createAccount(createAccountReqDTO)
    }

    override suspend fun createUser(user: User) {
        userLocalDataSource.insertUser(user)
    }

    override suspend fun updateAccessPin(newAccessPin: String, firebaseId: String) {
        userLocalDataSource.updateAccessPin(newAccessPin, firebaseId)
    }

    override fun createdUserPref() {
        sharedPreferencesManager.putInt(
            Constants.SharedPreferences.PREF_ACCOUNT_STATUS,
            Constants.AccountStatus.ACCOUNT_CREATED.id
        )
    }

    fun get422Error(response: Response<CreateAccountResDTO>): ArrayList<String> {
        val moshi = Moshi.Builder().build()

        val adapter = moshi.adapter(CreateAccount422DTO::class.java)

        val enterCodeError = adapter.fromJson(response.errorBody()!!.string())

        return WebServiceUtils.get422Errors(enterCodeError!!)
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