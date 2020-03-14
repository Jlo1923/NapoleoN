package com.naposystems.pepito.repository.accessPin

import com.naposystems.pepito.BuildConfig
import com.naposystems.pepito.db.dao.user.UserLocalDataSource
import com.naposystems.pepito.dto.accessPin.CreateAccount422DTO
import com.naposystems.pepito.dto.accessPin.CreateAccountErrorDTO
import com.naposystems.pepito.dto.accessPin.CreateAccountReqDTO
import com.naposystems.pepito.dto.accessPin.CreateAccountResDTO
import com.naposystems.pepito.entity.User
import com.naposystems.pepito.ui.register.accessPin.IContractAccessPin
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.Crypto
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.utility.WebServiceUtils
import com.naposystems.pepito.webService.NapoleonApi
import com.squareup.moshi.Moshi
import retrofit2.Response
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.collections.ArrayList

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

    override suspend fun setFreeTrialPref() {
        val firebaseId = sharedPreferencesManager.getString(
            Constants.SharedPreferences.PREF_FIREBASE_ID, ""
        )
        val user = userLocalDataSource.getUser(firebaseId)
        val createAtMiliseconds = TimeUnit.SECONDS.toMillis(user.createAt)
        val calendar = Calendar.getInstance()

        calendar.time = Date(createAtMiliseconds)

        when (user.type) {
            Constants.UserType.NEW_USER.type -> {
                calendar.add(Calendar.DAY_OF_YEAR, Constants.FreeTrialUsers.FORTY_FIVE_DAYS.time)
            }
            Constants.UserType.OLD_USER.type -> {
                calendar.add(Calendar.MONTH, Constants.FreeTrialUsers.THREE_MONTHS.time)
            }
        }
        sharedPreferencesManager.putLong(
            Constants.SharedPreferences.PREF_FREE_TRIAL, calendar.timeInMillis
        )
    }

    override fun saveSecretKey(secretKey: String) {

        val crypto = Crypto()

        sharedPreferencesManager.putString(
            Constants.SharedPreferences.PREF_SECRET_KEY,
            crypto.decryptCipherTextWithRandomIV(secretKey, BuildConfig.KEY_OF_KEYS)
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