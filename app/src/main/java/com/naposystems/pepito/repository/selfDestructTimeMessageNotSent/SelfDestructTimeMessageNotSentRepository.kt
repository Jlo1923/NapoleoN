package com.naposystems.pepito.repository.selfDestructTimeMessageNotSent

import com.naposystems.pepito.ui.selfDestructTimeMessageNotSentFragment.IContractSelfDestructTimeMessageNotSent
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.SharedPreferencesManager
import javax.inject.Inject

class SelfDestructTimeMessageNotSentRepository @Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager
) : IContractSelfDestructTimeMessageNotSent.Repository {

    override suspend fun getSelfDestructTimeMessageNotSent(): Int {
        return sharedPreferencesManager.getInt(
            Constants.SharedPreferences.PREF_MESSAGE_SELF_DESTRUCT_TIME_NOT_SENT
        )
    }

    override suspend fun setSelfDestructTimeMessageNotSent(time: Int) {
        sharedPreferencesManager.putInt(
            Constants.SharedPreferences.PREF_MESSAGE_SELF_DESTRUCT_TIME_NOT_SENT, time
        )
    }


}