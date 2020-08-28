package com.naposystems.napoleonchat.utility

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothProfile.ServiceListener
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Build
import androidx.annotation.RequiresApi
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

class BluetoothStateManager(
    private val context: Context,
    listener: BluetoothStateListener?
) {
    private enum class ScoConnection {
        DISCONNECTED, IN_PROGRESS, CONNECTED
    }

    private val LOCK = Any()
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothScoReceiver: BluetoothScoReceiver?
    private var bluetoothConnectionReceiver: BluetoothConnectionReceiver?
    private val listener: BluetoothStateListener?
    private val destroyed: AtomicBoolean
    private var bluetoothHeadset: BluetoothHeadset? = null
    private var scoConnection = ScoConnection.DISCONNECTED
    private var wantsConnection = false

    init {
        bluetoothScoReceiver = BluetoothScoReceiver()
        bluetoothConnectionReceiver = BluetoothConnectionReceiver()
        this.listener = listener
        destroyed = AtomicBoolean(false)
        requestHeadsetProxyProfile()
        this.context.registerReceiver(
            bluetoothConnectionReceiver,
            IntentFilter(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)
        )
        val sticky =
            this.context.registerReceiver(bluetoothScoReceiver, IntentFilter(scoChangeIntent))
        if (sticky != null) {
            bluetoothScoReceiver!!.onReceive(context, sticky)
        }
        handleBluetoothStateChange()
    }

    fun onDestroy() {
        destroyed.set(true)
        if (bluetoothHeadset != null && bluetoothAdapter != null) {
            bluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, bluetoothHeadset)
        }
        if (bluetoothConnectionReceiver != null) {
            context.unregisterReceiver(bluetoothConnectionReceiver)
            bluetoothConnectionReceiver = null
        }
        if (bluetoothScoReceiver != null) {
            context.unregisterReceiver(bluetoothScoReceiver)
            bluetoothScoReceiver = null
        }
        bluetoothHeadset = null
    }

    fun setWantsConnection(enabled: Boolean) {
        synchronized(LOCK) {
            val audioManager: AudioManager = Utils.getAudioManager(context)
            wantsConnection = enabled
            if (wantsConnection && isBluetoothAvailable && scoConnection == ScoConnection.DISCONNECTED) {
                audioManager.startBluetoothSco()
                scoConnection = ScoConnection.IN_PROGRESS
            } else if (!wantsConnection && scoConnection == ScoConnection.CONNECTED) {
                audioManager.stopBluetoothSco()
                audioManager.isBluetoothScoOn = false
                scoConnection = ScoConnection.DISCONNECTED
            } else if (!wantsConnection && scoConnection == ScoConnection.IN_PROGRESS) {
                audioManager.stopBluetoothSco()
                scoConnection = ScoConnection.DISCONNECTED
            }
        }
    }

    private fun handleBluetoothStateChange() {
        if (listener != null && !destroyed.get())
            listener.onBluetoothStateChanged(isBluetoothAvailable)
    }

    private val isBluetoothAvailable: Boolean
        get() = try {
            synchronized(LOCK) {
                val audioManager: AudioManager = Utils.getAudioManager(context)
                if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) return false
                if (!audioManager.isBluetoothScoAvailableOffCall) return false
                return bluetoothHeadset != null && bluetoothHeadset?.connectedDevices?.isNotEmpty() ?: false
            }
        } catch (e: Exception) {
            Timber.w(e)
            false
        }

    private val scoChangeIntent: String
        get() = AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED

    private fun requestHeadsetProxyProfile() {
        try {
            bluetoothAdapter?.getProfileProxy(context, object : ServiceListener {
                @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
                override fun onServiceConnected(
                    profile: Int,
                    proxy: BluetoothProfile
                ) {
                    if (destroyed.get()) {
                        Timber.w("Got bluetooth profile event after the service was destroyed. Ignoring.")
                        return
                    }
                    if (profile == BluetoothProfile.HEADSET) {
                        synchronized(LOCK) { bluetoothHeadset = proxy as BluetoothHeadset }
                        val sticky =
                            context.registerReceiver(null, IntentFilter(scoChangeIntent))
                        sticky?.let {
                            bluetoothScoReceiver?.onReceive(context, sticky)
                        }
                        synchronized(LOCK) {
                            if (wantsConnection && isBluetoothAvailable && scoConnection == ScoConnection.DISCONNECTED) {
                                val audioManager: AudioManager = Utils.getAudioManager(context)
                                audioManager.startBluetoothSco()
                                scoConnection = ScoConnection.IN_PROGRESS
                            }
                        }
                        handleBluetoothStateChange()
                    }
                }

                override fun onServiceDisconnected(profile: Int) {
                    Timber.i("onServiceDisconnected")
                    if (profile == BluetoothProfile.HEADSET) {
                        bluetoothHeadset = null
                        handleBluetoothStateChange()
                    }
                }
            }, BluetoothProfile.HEADSET)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private inner class BluetoothScoReceiver : BroadcastReceiver() {
        override fun onReceive(
            context: Context,
            intent: Intent
        ) {
            Timber.i("onReceive")
            synchronized(LOCK) {
                if (scoChangeIntent == intent.action) {
                    val status = intent.getIntExtra(
                        AudioManager.EXTRA_SCO_AUDIO_STATE,
                        AudioManager.SCO_AUDIO_STATE_ERROR
                    )
                    if (status == AudioManager.SCO_AUDIO_STATE_CONNECTED) {
                        if (bluetoothHeadset != null) {
                            val devices =
                                bluetoothHeadset!!.connectedDevices
                            for (device in devices) {
                                if (bluetoothHeadset!!.isAudioConnected(device)) {
                                    val deviceClass =
                                        device.bluetoothClass.deviceClass
                                    if (deviceClass == BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE ||
                                        deviceClass == BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO ||
                                        deviceClass == BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET
                                    ) {
                                        scoConnection = ScoConnection.CONNECTED
                                        if (wantsConnection) {
                                            val audioManager: AudioManager =
                                                Utils.getAudioManager(context)
                                            audioManager.isBluetoothScoOn = true
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            handleBluetoothStateChange()
        }
    }

    private inner class BluetoothConnectionReceiver :
        BroadcastReceiver() {
        override fun onReceive(
            context: Context,
            intent: Intent
        ) {
            Timber.i("onReceive")
            handleBluetoothStateChange()
        }
    }

    interface BluetoothStateListener {
        fun onBluetoothStateChanged(isAvailable: Boolean)
    }
}