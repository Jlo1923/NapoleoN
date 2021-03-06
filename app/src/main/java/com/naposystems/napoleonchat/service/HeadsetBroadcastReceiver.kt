package com.naposystems.napoleonchat.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.utility.Constants
import timber.log.Timber

class HeadsetBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action.equals(Intent.ACTION_HEADSET_PLUG)) {
            val headsetType = intent?.getStringExtra("name") ?: ""

            Timber.d("headsetType: $headsetType")

            when (intent?.getIntExtra("state", Constants.HeadsetState.UNKNOWN.state)) {
                Constants.HeadsetState.UNPLUGGED.state ->
                    RxBus.publish(
                        RxEvent.HeadsetState(
                            Constants.HeadsetState.UNPLUGGED.state
                        )
                    )

                Constants.HeadsetState.PLUGGED.state ->
                    RxBus.publish(RxEvent.HeadsetState(Constants.HeadsetState.PLUGGED.state))

                else -> Timber.d("Error")
            }
        }
    }
}