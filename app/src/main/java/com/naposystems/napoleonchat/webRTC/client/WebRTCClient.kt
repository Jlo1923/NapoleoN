package com.naposystems.napoleonchat.webRTC.client

import android.widget.TextView
import com.naposystems.napoleonchat.model.CallModel
import org.webrtc.SurfaceViewRenderer

interface WebRTCClient {

    var callModel: CallModel

    var renegotiateCall: Boolean

    var isActiveCall: Boolean

    var isHideVideo: Boolean

    var contactCameraIsVisible: Boolean

    var isMicOn: Boolean

    var isBluetoothActive: Boolean

    fun reInit()

    fun setWebRTCClientListener(webRTCClientListener: WebRTCClientListener)

    fun connectSocket(mustSubscribeToPresenceChannel: Boolean, callModel: CallModel)

    fun subscribeToPresenceChannel()

    fun setOffer(offer: String?)

    fun createAnswer()

    fun startWebRTCService(callModel: CallModel)

    //Change to video Call
    fun changeToVideoCall()
    fun meAcceptChangeToVideoCall()
    fun meCancelChangeToVideoCall()

    //Video
    fun initSurfaceRenders()
    fun startCaptureVideo()
    fun setLocalVideoView(surfaceViewRenderer: SurfaceViewRenderer)
    fun setRemoteVideoView(surfaceViewRenderer: SurfaceViewRenderer)
    fun renderRemoteVideo()

    //Camera
    fun toggleVideo(checked: Boolean, itsFromBackPressed: Boolean = false)
    fun switchCamera()

    //Ringtone
    fun playRingtone()
    fun playRingBackTone()
    fun stopRingAndVibrate()

    //Sensor Proximity
    fun startProximitySensor()
    fun stopProximitySensor()

    //Speaker
    fun setSpeakerOn(isChecked: Boolean)
    fun isSpeakerOn(): Boolean

    //Microphone
    fun setMicOff()

    //Bluetooth
    fun handleBluetooth(isEnabled: Boolean)

    //KeyDown
    fun handleKeyDown(keyCode: Int): Boolean

    //UI
    fun setTextViewCallDuration(textView: TextView)

    fun setItsReturnCall(itsReturnCall: Boolean)

    //Hang Up
    fun emitHangUp()
    fun hideNotification()
    fun disposeCall(callModel: CallModel? = null)

}