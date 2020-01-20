package com.naposystems.pepito.repository.selfDestructTime

import com.naposystems.pepito.ui.selfDestructTime.IContractSelfDestructTime
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.SharedPreferencesManager
import javax.inject.Inject

class SelfDestructTimeRepository @Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager
) : IContractSelfDestructTime.Repository {

    override fun getSelfDestructTime(): Int {
        return sharedPreferencesManager.getInt(Constants.SharedPreferences.PREF_SELF_DESTRUCT_TIME)
    }

    override fun setSelfDestructTime(selfDestructTime: Int) {
        sharedPreferencesManager.putInt(
            Constants.SharedPreferences.PREF_SELF_DESTRUCT_TIME,
            selfDestructTime
        )
    }
}