package com.naposystems.pepito.service.phoneState

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import com.naposystems.pepito.utility.SharedPreferencesManager
import dagger.android.support.DaggerApplication
import javax.inject.Inject

class PhoneStateBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var sharedPreferencesManager: SharedPreferencesManager

    override fun onReceive(context: Context, intent: Intent?) {
        (context.applicationContext as DaggerApplication).androidInjector().inject(this)

        val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        telephonyManager.listen(
            CustomPhoneStateListener(context, sharedPreferencesManager),
            PhoneStateListener.LISTEN_CALL_STATE
        )
    }
}