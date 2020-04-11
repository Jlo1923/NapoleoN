package com.naposystems.pepito.webRTC

import android.widget.TextView
import org.webrtc.SurfaceViewRenderer

interface IContractWebRTCClient {
    fun setListener(webRTCClientListener: WebRTCClient.WebRTCClientListener)
    fun setIsVideoCall(isVideoCall: Boolean)
    fun setChannel(channel: String)
    fun subscribeToChannel()
    fun setTextViewTitle(textView: TextView)
    fun setLocalVideoView(surfaceViewRenderer: SurfaceViewRenderer)
    fun setRemoteVideoView(surfaceViewRenderer: SurfaceViewRenderer)
    fun setSpeakerOn()
    fun setMicOff()
    fun initSurfaceRenders()
    fun startCaptureVideo()
    fun emitJoinToCall()
    fun stopRingAndVibrate()
    fun emitHangUp()
    fun changeToVideoCall()
    fun muteVideo(checked: Boolean)
    fun switchCamera()
    fun handleBluetooth(isEnabled: Boolean)
    fun playRingtone()
    fun playCallingTone()
    fun acceptChangeToVideoCall()
    fun startProximitySensor()
    fun stopProximitySensor()
    fun handleKeyDown(keyCode: Int): Boolean
    fun dispose()
}