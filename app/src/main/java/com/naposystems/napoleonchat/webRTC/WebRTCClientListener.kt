package com.naposystems.napoleonchat.webRTC

import android.widget.TextView
import com.pusher.client.channel.PrivateChannel
import org.webrtc.SurfaceViewRenderer

interface WebRTCClientListener {
    fun contactWantChangeToVideoCall()
    fun contactCancelledVideoCall()
    fun contactTurnOffCamera()
    fun contactTurnOnCamera()
    fun showRemoteVideo()
    fun callEnded()
    fun changeLocalRenderVisibility(visibility: Int)
    fun changeTextViewTitle(stringResourceId: Int)
    fun changeBluetoothButtonVisibility(isVisible: Boolean)
    fun enableControls()
    fun resetIsOnCallPref()
    fun contactNotAnswer()
    fun showTimer()
    fun showConnectingTitle()
    fun changeCheckedSpeaker(checked: Boolean)
    fun hangupByNotification()
    fun unlockVideoButton()
    fun rejectByNotification()
}