package com.naposystems.pepito.repository.profile

import androidx.lifecycle.LiveData
import com.naposystems.pepito.db.dao.user.UserLocalDataSource
import com.naposystems.pepito.dto.profile.UpdateUserInfo422DTO
import com.naposystems.pepito.dto.profile.UpdateUserInfoErrorDTO
import com.naposystems.pepito.dto.profile.UpdateUserInfoReqDTO
import com.naposystems.pepito.dto.profile.UpdateUserInfoResDTO
import com.naposystems.pepito.entity.User
import com.naposystems.pepito.ui.profile.IContractProfile
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.utility.WebServiceUtils
import com.naposystems.pepito.webService.NapoleonApi
import com.squareup.moshi.Moshi
import retrofit2.Response
import timber.log.Timber
import java.lang.reflect.Field
import javax.inject.Inject
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties


class ProfileRepository @Inject constructor(
    private val localDataSource: UserLocalDataSource,
    private val sharedPreferencesManager: SharedPreferencesManager
) :
    IContractProfile.Repository {

    override suspend fun getUser(): LiveData<User> {
        return localDataSource.getUserLiveData(
            sharedPreferencesManager.getString(
                Constants.SharedPreferences.PREF_FIREBASE_ID,
                ""
            )
        )
    }

    override suspend fun updateLocalUser(user: User) {
        localDataSource.updateUser(user)
    }

}