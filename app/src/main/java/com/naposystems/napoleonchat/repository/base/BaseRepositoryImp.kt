package com.naposystems.napoleonchat.repository.base

import com.naposystems.napoleonchat.service.socketClient.SocketClient
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import timber.log.Timber
import javax.inject.Inject

class BaseRepositoryImp @Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val socketClient: SocketClient
) : BaseRepository {

    override suspend fun outputControl(state: Int) {
        sharedPreferencesManager.putInt(
            Constants.SharedPreferences.PREF_OUTPUT_CONTROL, state
        )
    }

    override suspend fun getOutputControl(): Int {
        return sharedPreferencesManager.getInt(
            Constants.SharedPreferences.PREF_OUTPUT_CONTROL
        )
    }

    override fun connectSocket() {
        Timber.d("LLAMADA PASO 3: BASE REPOSITORY")
        socketClient.connectSocket()
    }
}