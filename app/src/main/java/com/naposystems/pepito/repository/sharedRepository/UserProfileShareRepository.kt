package com.naposystems.pepito.repository.sharedRepository

import androidx.lifecycle.LiveData
import com.naposystems.pepito.db.dao.user.UserLocalDataSource
import com.naposystems.pepito.dto.profile.UpdateUserInfo422DTO
import com.naposystems.pepito.dto.profile.UpdateUserInfoErrorDTO
import com.naposystems.pepito.dto.profile.UpdateUserInfoResDTO
import com.naposystems.pepito.entity.User
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.utility.WebServiceUtils
import com.naposystems.pepito.utility.sharedViewModels.userProfile.IContractUserProfileShare
import com.naposystems.pepito.webService.NapoleonApi
import com.squareup.moshi.Moshi
import retrofit2.Response
import javax.inject.Inject

class UserProfileShareRepository @Inject constructor(
    private val napoleonApi: NapoleonApi,
    private val userLocalDataSource: UserLocalDataSource,
    private val sharedPreferencesManager: SharedPreferencesManager
) : IContractUserProfileShare.Repository {

    private val moshi by lazy {
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

    fun get422Error(response: Response<UpdateUserInfoResDTO>): ArrayList<String> {
        val adapter = moshi.adapter(UpdateUserInfo422DTO::class.java)
        val enterCodeError = adapter.fromJson(response.errorBody()!!.string())
        return WebServiceUtils.get422Errors(enterCodeError!!)
    }

    fun getDefaultError(response: Response<UpdateUserInfoResDTO>): ArrayList<String> {
        val adapter = moshi.adapter(UpdateUserInfoErrorDTO::class.java)
        val updateUserInfoError = adapter.fromJson(response.errorBody()!!.string())
        return arrayListOf(updateUserInfoError!!.error)
    }

}