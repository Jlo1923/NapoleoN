package com.naposystems.pepito.repository.activateBiometrics

import com.naposystems.pepito.ui.activateBiometrics.IContractActivateBiometrics
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.SharedPreferencesManager
import javax.inject.Inject

class ActivateBiometricsRepository @Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager
) : IContractActivateBiometrics.Repository {

    override suspend fun getBiometricsOption(): Int {
        return sharedPreferencesManager.getInt(Constants.SharedPreferences.PREF_BIOMETRICS_OPTION)
    }

    override suspend fun setBiometricsOption(option: Int) {
        sharedPreferencesManager.putInt(
            Constants.SharedPreferences.PREF_BIOMETRICS_OPTION, option)
    }
}
