package com.naposystems.napoleonchat.repository.sharedRepository

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.source.local.datasource.user.UserLocalDataSourceImp
import com.naposystems.napoleonchat.source.remote.dto.profile.UpdateUserInfo422DTO
import com.naposystems.napoleonchat.source.remote.dto.profile.UpdateUserInfoErrorDTO
import com.naposystems.napoleonchat.source.remote.dto.profile.UpdateUserInfoResDTO
import com.naposystems.napoleonchat.source.local.entity.UserEntity
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.utility.WebServiceUtils
import com.naposystems.napoleonchat.utility.sharedViewModels.userProfile.IContractUserProfileShare
import com.naposystems.napoleonchat.source.remote.api.NapoleonApi
import com.squareup.moshi.Moshi
import retrofit2.Response
import javax.inject.Inject

class UserProfileShareRepository @Inject constructor(
    private val napoleonApi: NapoleonApi,
    private val userLocalDataSourceImp: UserLocalDataSourceImp,
    private val sharedPreferencesManager: SharedPreferencesManager
) : IContractUserProfileShare.Repository {

    private val moshi: Moshi by lazy {
        Moshi.Builder().build()
    }

    override suspend fun getUser(): LiveData<UserEntity> {
        val firebaseId = sharedPreferencesManager.getString(
            Constants.SharedPreferences.PREF_FIREBASE_ID, ""
        )
        return userLocalDataSourceImp.getUserLiveData(firebaseId)
    }

    override suspend fun updateUserInfo(updateUserInfoReqDTO: Any): Response<UpdateUserInfoResDTO> {
        return napoleonApi.updateUserInfo(updateUserInfoReqDTO)
    }

    override suspend fun updateUserLocal(userEntity: UserEntity) {
        userLocalDataSourceImp.updateUser(userEntity)
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