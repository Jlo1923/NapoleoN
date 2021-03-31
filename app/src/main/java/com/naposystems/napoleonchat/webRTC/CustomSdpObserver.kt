package com.naposystems.napoleonchat.webRTC

import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import timber.log.Timber

open class CustomSdpObserver
constructor(
    private val label: String
) : SdpObserver {

    override fun onSetFailure(s: String) {
        Timber.d("$label onSetFailure() called with: s = [$s]")
    }

    override fun onSetSuccess() {
        Timber.d("$label onSetSuccess() called")
    }

    override fun onCreateSuccess(sessionDescription: SessionDescription) {
        Timber.d("$label onCreateSuccess() called with: sessionDescription = [$sessionDescription]")
    }

    override fun onCreateFailure(s: String) {
        Timber.d("$label onCreateFailure() called with: s = [$s]")
    }
}