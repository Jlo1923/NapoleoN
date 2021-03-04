package com.naposystems.napoleonchat.repository.sharedRepository

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.db.dao.user.UserLocalDataSource
import com.naposystems.napoleonchat.dto.profile.UpdateUserInfoUnprocessableEntityDTO
import com.naposystems.napoleonchat.dto.profile.UpdateUserInfoErrorDTO
import com.naposystems.napoleonchat.dto.profile.UpdateUserInfoResDTO
import com.naposystems.napoleonchat.entity.User
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.utility.WebServiceUtils
import com.naposystems.napoleonchat.utility.sharedViewModels.userProfile.IContractUserProfileShare
import com.naposystems.napoleonchat.webService.NapoleonApi
import com.squareup.moshi.Moshi
import retrofit2.Response
import javax.inject.Inject

class UserProfileShareRepository @Inject constructor(
    private val napoleonApi: NapoleonApi,
    private val userLocalDataSource: UserLocalDataSource,
    private val sharedPreferencesManager: SharedPreferencesManager
) : IContractUserProfileShare.Repository {

    private val moshi: Moshi by lazy {
        Moshi.Builder().build()
    }

    override suspend fun getUser(): LiveData<User> {
        val firebaseId = sharedPreferencesManager.getString(
            Constants.SharedPreferences.PREF_FIREBASE_ID, ""
        )
        return userLocalDataSource.getUserLiveData(firebaseId)
    }

    override suspend fun updateUserInfo(updateUserInfoReqDTO: Any): Response<UpdateUserInfoResDTO> {
        return napoleonApi.updateUserInfo(updateUserInfoReqDTO)
    }

    override suspend fun updateUserLocal(user: User) {
        userLocalDataSource.updateUser(user)
    }

    fun getUnprocessableEntityError(response: Response<UpdateUserInfoResDTO>): ArrayList<String> {
        val adapter = moshi.adapter(UpdateUserInfoUnprocessableEntityDTO::class.java)
        val enterCodeError = adapter.fromJson(response.errorBody()!!.string())
        return WebServiceUtils.getUnprocessableEntityErrors(enterCodeError!!)
    }

    fun getDefaultError(response: Response<UpdateUserInfoResDTO>): ArrayList<String> {
        val adapter = moshi.adapter(UpdateUserInfoErrorDTO::class.java)
        val updateUserInfoError = adapter.fromJson(response.errorBody()!!.string())
        return arrayListOf(updateUserInfoError!!.error)
    }

}