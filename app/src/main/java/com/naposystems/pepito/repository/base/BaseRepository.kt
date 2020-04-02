package com.naposystems.pepito.repository.base

import com.naposystems.pepito.ui.baseFragment.IContractBase
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.SharedPreferencesManager
import javax.inject.Inject

class BaseRepository @Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager
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
}