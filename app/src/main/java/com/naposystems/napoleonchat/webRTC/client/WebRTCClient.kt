package com.naposystems.napoleonchat.webRTC.client

import android.widget.TextView
import com.naposystems.napoleonchat.utility.TypeEndCallEnum
import org.webrtc.SurfaceViewRenderer

interface WebRTCClient {

    var renegotiateCall: Boolean

    var isHideVideo: Boolean

    var contactCameraIsVisible: Boolean

    var isMicOn: Boolean

    var isBluetoothActive: Boolean

    fun reInit()

    fun setEventsFromWebRTCClientListener(evenstFromWebRTCClientListener: EvenstFromWebRTCClientListener)

    fun connectSocket()

    fun subscribeToPresenceChannel()

    fun setOffer()

    fun createAnswer()

    fun startWebRTCService()

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

    //Hang Up
    fun emitHangUp()
    fun hideNotification()
    fun disposeCall(typeEndCall: TypeEndCallEnum? = null)

    fun rejectCall()
    fun rejectSecondCall(contactId: Int, channelName: String)

}