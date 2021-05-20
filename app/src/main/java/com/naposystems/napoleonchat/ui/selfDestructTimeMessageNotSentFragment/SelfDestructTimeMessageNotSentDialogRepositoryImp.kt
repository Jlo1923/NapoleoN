package com.naposystems.napoleonchat.ui.selfDestructTimeMessageNotSentFragment

import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import javax.inject.Inject

class SelfDestructTimeMessageNotSentDialogRepositoryImp @Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager
) : SelfDestructTimeMessageNotSentDialogRepository {

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