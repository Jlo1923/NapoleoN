package com.naposystems.napoleonchat.repository.profile

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.service.socketClient.SocketClient
import com.naposystems.napoleonchat.source.local.datasource.user.UserLocalDataSourceImp
import com.naposystems.napoleonchat.source.local.entity.UserEntity
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import javax.inject.Inject

class ProfileRepositoryImp
@Inject constructor(
    private val localDataSourceImp: UserLocalDataSourceImp,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val socketClient: SocketClient
) : ProfileRepository {

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
        socketClient.disconnectSocket()
    }
}