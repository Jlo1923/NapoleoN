package com.naposystems.napoleonchat.repository.profile

import android.content.Context
import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.db.dao.user.UserLocalDataSource
import com.naposystems.napoleonchat.entity.User
import com.naposystems.napoleonchat.ui.profile.IContractProfile
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.webService.socket.IContractSocketService
import javax.inject.Inject


class ProfileRepository @Inject constructor(
    private val context: Context,
    private val localDataSource: UserLocalDataSource,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val socketService: IContractSocketService.SocketService
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

    override fun disconnectSocket() {
        socketService.disconnectSocket()
    }
}