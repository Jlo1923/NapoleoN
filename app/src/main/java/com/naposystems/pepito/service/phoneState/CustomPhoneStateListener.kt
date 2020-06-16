package com.naposystems.pepito.service.phoneState

import android.content.Context
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.SharedPreferencesManager


class CustomPhoneStateListener(
    var context: Context,
    var sharedPreferencesManager: SharedPreferencesManager
) : PhoneStateListener() {

    override fun onCallStateChanged(state: Int, incomingNumber: String) {
        super.onCallStateChanged(state, incomingNumber)
        when (state) {
            TelephonyManager.CALL_STATE_IDLE -> {
                //when Idle i.e no call
                sharedPreferencesManager.putBoolean(
                    Constants.SharedPreferences.PREF_IS_ON_CALL,
                    false
                )
            }
            TelephonyManager.CALL_STATE_OFFHOOK -> {
                //when Off hook i.e in call
                sharedPreferencesManager.putBoolean(
                    Constants.SharedPreferences.PREF_IS_ON_CALL,
                    true
                )
            }
            TelephonyManager.CALL_STATE_RINGING -> {
                //when Ringing
                sharedPreferencesManager.putBoolean(
                    Constants.SharedPreferences.PREF_IS_ON_CALL,
                    true
                )
            }
        }
    }
}