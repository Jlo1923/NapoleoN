package com.naposystems.napoleonchat.repository.profile

import android.content.Context
import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.source.local.datasource.user.UserLocalDataSourceImp
import com.naposystems.napoleonchat.source.local.entity.UserEntity
import com.naposystems.napoleonchat.ui.profile.IContractProfile
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.service.socketMessage.SocketMessageService
import javax.inject.Inject


class ProfileRepository @Inject constructor(
    private val context: Context,
    private val localDataSourceImp: UserLocalDataSourceImp,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val socketMessageService: SocketMessageService
) :
    IContractProfile.Repository {

    override suspend fun getUser(): LiveData<UserEntity> {
        return localDataSourceImp.getUserLiveData(
            sharedPreferencesManager.getString(
                Constants.SharedPreferences.PREF_FIREBASE_ID,
                ""
            )
        )
    }

    override suspend fun updateLocalUser(userEntity: UserEntity) {
        localDataSourceImp.updateUser(userEntity)
    }

    override fun disconnectSocket() {
        socketMessageService.disconnectSocket()
    }
}