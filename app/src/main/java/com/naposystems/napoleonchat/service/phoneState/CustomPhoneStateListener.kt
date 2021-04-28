package com.naposystems.napoleonchat.service.phoneState

import android.content.Context
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import com.naposystems.napoleonchat.app.NapoleonApplication
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.utility.StatusCallEnum


class CustomPhoneStateListener(
    var context: Context,
    var sharedPreferencesManager: SharedPreferencesManager
) : PhoneStateListener() {

    override fun onCallStateChanged(state: Int, incomingNumber: String) {
        super.onCallStateChanged(state, incomingNumber)
        when (state) {
            TelephonyManager.CALL_STATE_IDLE -> {
                //when Idle i.e no call
                NapoleonApplication.statusCall = StatusCallEnum.STATUS_NO_CALL
            }
            TelephonyManager.CALL_STATE_OFFHOOK -> {
                //when Off hook i.e in call
                NapoleonApplication.statusCall = StatusCallEnum.STATUS_CONNECTED_CALL
            }
            TelephonyManager.CALL_STATE_RINGING -> {
                //when Ringing
                RxBus.publish(
                    RxEvent.IncomingCallSystem()
                )
                NapoleonApplication.statusCall = StatusCallEnum.STATUS_CONNECTED_CALL
            }
        }
    }
}