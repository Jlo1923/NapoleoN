package com.naposystems.napoleonchat.repository.accessPin

import com.naposystems.napoleonchat.BuildConfig
import com.naposystems.napoleonchat.crypto.Crypto
import com.naposystems.napoleonchat.db.dao.user.UserLocalDataSource
import com.naposystems.napoleonchat.dto.accessPin.CreateAccount422DTO
import com.naposystems.napoleonchat.dto.accessPin.CreateAccountErrorDTO
import com.naposystems.napoleonchat.dto.accessPin.CreateAccountReqDTO
import com.naposystems.napoleonchat.dto.accessPin.CreateAccountResDTO
import com.naposystems.napoleonchat.entity.User
import com.naposystems.napoleonchat.ui.register.accessPin.IContractAccessPin
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.utility.WebServiceUtils
import com.naposystems.napoleonchat.webService.NapoleonApi
import com.squareup.moshi.Moshi
import retrofit2.Response
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.collections.ArrayList

class CreateAccountRepository @Inject constructor(
    private val moshi: Moshi,
    private val crypto: Crypto,
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
        sharedPreferencesManager.putInt(Constants.SharedPreferences.PREF_USER_ID, user.id)
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

    override suspend fun setFreeTrialPref(subscription: Boolean) {
        if (subscription) {
            sharedPreferencesManager.putLong(
                Constants.SharedPreferences.PREF_FREE_TRIAL, 0
            )
        } else {
            val firebaseId = sharedPreferencesManager.getString(
                Constants.SharedPreferences.PREF_FIREBASE_ID, ""
            )
            val createAtMilliseconds = TimeUnit.SECONDS.toMillis(
                userLocalDataSource.getUser(firebaseId).createAt
            )

            val calendar = Calendar.getInstance()
            calendar.time = Date(createAtMilliseconds)
            calendar.add(Calendar.DAY_OF_YEAR, Constants.FreeTrialUsers.THIRTY_DAYS.time)

            sharedPreferencesManager.putLong(
                Constants.SharedPreferences.PREF_FREE_TRIAL, calendar.timeInMillis
            )
        }
    }

    override fun saveSecretKey(secretKey: String) {

        sharedPreferencesManager.putString(
            Constants.SharedPreferences.PREF_SECRET_KEY,
            crypto.decryptCipherTextWithRandomIV(secretKey, BuildConfig.KEY_OF_KEYS)
        )
    }


    fun get422Error(response: Response<CreateAccountResDTO>): ArrayList<String> {

        val adapter = moshi.adapter(CreateAccount422DTO::class.java)

        val enterCodeError = adapter.fromJson(response.errorBody()!!.string())

        return WebServiceUtils.get422Errors(enterCodeError!!)
    }

    fun getError(response: Response<CreateAccountResDTO>): ArrayList<String> {

        val adapter = moshi.adapter(CreateAccountErrorDTO::class.java)

        val enterCodeError = adapter.fromJson(response.errorBody()!!.string())

        val errorList = ArrayList<String>()

        errorList.add(enterCodeError!!.error)

        return errorList
    }
}