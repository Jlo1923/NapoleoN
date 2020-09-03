package com.naposystems.napoleonchat.webRTC

import android.widget.TextView
import org.webrtc.SurfaceViewRenderer

interface IContractWebRTCClient {
    fun setListener(webRTCClientListener: WebRTCClient.WebRTCClientListener)
    fun setIsVideoCall(isVideoCall: Boolean)
    fun setIncomingCall(incomingCall: Boolean)
    fun setChannel(channel: String)
    fun subscribeToChannel()
    fun setTextViewCallDuration(textView: TextView)
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
    fun isActiveCall(): Boolean
    fun dispose()
    fun unSubscribeCallChannel()
    fun subscribeToChannelFromBackground()
}