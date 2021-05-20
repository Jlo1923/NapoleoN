package com.naposystems.napoleonchat.repository.accessPin

import com.naposystems.napoleonchat.BuildConfig
import com.naposystems.napoleonchat.crypto.Crypto
import com.naposystems.napoleonchat.source.local.datasource.user.UserLocalDataSourceImp
import com.naposystems.napoleonchat.source.local.entity.UserEntity
import com.naposystems.napoleonchat.source.remote.api.NapoleonApi
import com.naposystems.napoleonchat.source.remote.dto.accessPin.CreateAccountErrorDTO
import com.naposystems.napoleonchat.source.remote.dto.accessPin.CreateAccountReqDTO
import com.naposystems.napoleonchat.source.remote.dto.accessPin.CreateAccountResDTO
import com.naposystems.napoleonchat.source.remote.dto.accessPin.CreateAccountUnprocessableEntityDTO
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.utility.WebServiceUtils
import com.squareup.moshi.Moshi
import retrofit2.Response
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.collections.ArrayList

class AccessPinRepositoryImp @Inject constructor(
    private val userLocalDataSourceImp: UserLocalDataSourceImp,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val napoleonApi: NapoleonApi
) : AccessPinRepository {

    private val moshi by lazy {
        Moshi.Builder().build()
    }

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

    override suspend fun createUser(userEntity: UserEntity) {
        userLocalDataSourceImp.insertUser(userEntity)
//        sharedPreferencesManager.putInt(Constants.SharedPreferences.PREF_USER_ID, user.id)
    }

    override suspend fun updateAccessPin(newAccessPin: String, firebaseId: String) {
        userLocalDataSourceImp.updateAccessPin(newAccessPin, firebaseId)
    }

    override fun createdUserPref() {

        Timber.d("AccountStatus createdUserPref ${Constants.AccountStatus.ACCOUNT_CREATED.id}")

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
                userLocalDataSourceImp.getMyUser().createAt
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

        val crypto = Crypto()

        sharedPreferencesManager.putString(
            Constants.SharedPreferences.PREF_SECRET_KEY,
            crypto.decryptCipherTextWithRandomIV(secretKey, BuildConfig.KEY_OF_KEYS)
        )
    }


    fun getUnprocessableEntityError(response: Response<CreateAccountResDTO>): ArrayList<String> {

        val adapter = moshi.adapter(CreateAccountUnprocessableEntityDTO::class.java)

        val enterCodeError = adapter.fromJson(response.errorBody()!!.string())

        return WebServiceUtils.getUnprocessableEntityErrors(enterCodeError!!)
    }

    fun getError(response: Response<CreateAccountResDTO>): ArrayList<String> {

        val adapter = moshi.adapter(CreateAccountErrorDTO::class.java)

        val enterCodeError = adapter.fromJson(response.errorBody()!!.string())

        val errorList = ArrayList<String>()

        errorList.add(enterCodeError!!.error)

        return errorList
    }
}