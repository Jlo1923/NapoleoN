package com.naposystems.napoleonchat.dialog.activateBiometrics

import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import javax.inject.Inject

class ActivateBiometricsDialogRepositoryImp @Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager
) : ActivateBiometricsDialogRepository {

    override suspend fun getBiometricsOption(): Int {
        return sharedPreferencesManager.getInt(Constants.SharedPreferences.PREF_BIOMETRICS_OPTION)
    }

    override suspend fun setBiometricsOption(option: Int) {
        sharedPreferencesManager.putInt(
            Constants.SharedPreferences.PREF_BIOMETRICS_OPTION, option
        )
    }
}
