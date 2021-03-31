package com.naposystems.napoleonchat.repository.base

import com.naposystems.napoleonchat.ui.baseFragment.IContractBase
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.service.socketMessage.SocketMessageService
import javax.inject.Inject

class BaseRepository @Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val socketMessageService: SocketMessageService
) : IContractBase.Repository {

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
        socketMessageService.connectSocket()
    }
}