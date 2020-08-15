package com.naposystems.napoleonchat.repository.logout

import android.content.Context
import com.naposystems.napoleonchat.db.dao.user.UserLocalDataSource
import com.naposystems.napoleonchat.dto.user.LogoutResDTO
import com.naposystems.napoleonchat.ui.logout.IContractLogout
import com.naposystems.napoleonchat.utility.FileManager
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.webService.NapoleonApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject

class LogoutRepository @Inject constructor(
    private val context: Context,
    private val napoleonApi: NapoleonApi,
    private val userLocalDataSource: UserLocalDataSource,
    private val sharedPreferencesManager: SharedPreferencesManager
) :
    IContractLogout.Repository {

    override suspend fun logOut(): Response<LogoutResDTO> {
        return napoleonApi.logout()
    }

    override suspend fun clearData() {
        sharedPreferencesManager.reset()
        userLocalDataSource.clearAllData()

        withContext(Dispatchers.IO) {
            FileManager.deleteAllFiles(context)
        }
    }
}