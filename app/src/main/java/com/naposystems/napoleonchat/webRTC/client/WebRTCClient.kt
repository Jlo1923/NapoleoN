package com.naposystems.napoleonchat.webRTC.client

import android.widget.TextView
import com.naposystems.napoleonchat.model.CallModel
import org.webrtc.SurfaceViewRenderer

interface WebRTCClient {

    var callModel: CallModel
    var isActiveCall: Boolean

    fun setWebRTCClientListener(webRTCClientListener: WebRTCClientListener)

    fun connectSocket(mustSubscribeToPresenceChannel: Boolean, callModel: CallModel)
    fun subscribeToPresenceChannel()
    fun setOffer(offer: String?)
    fun setTextViewCallDuration(textView: TextView)
    fun setLocalVideoView(surfaceViewRenderer: SurfaceViewRenderer)
    fun setRemoteVideoView(surfaceViewRenderer: SurfaceViewRenderer)
    fun setSpeakerOn(isChecked: Boolean)
    fun setMicOff()
    fun setItsReturnCall(itsReturnCall: Boolean)

    //TODO: Estas funciones se pueden cambiar a accesores de atributos
    fun getMicIsOn(): Boolean
    fun isSpeakerOn(): Boolean
    fun isVideoMuted(): Boolean
    fun isBluetoothActive(): Boolean

    fun contactTurnOffCamera(): Boolean
    fun initSurfaceRenders()
    fun startCaptureVideo()

    //    fun emitJoinToCall()
    fun stopRingAndVibrate()
    fun emitHangUp()
    fun changeToVideoCall()
    fun cancelChangeToVideoCall()
    fun muteVideo(checked: Boolean, itsFromBackPressed: Boolean = false)
    fun switchCamera()
    fun handleBluetooth(isEnabled: Boolean)
    fun playRingtone()
    fun playRingBackTone()
    fun acceptChangeToVideoCall()
    fun startProximitySensor()
    fun stopProximitySensor()
    fun handleKeyDown(keyCode: Int): Boolean
    fun disposeCall()
    fun unSubscribePresenceChannel()

    //    fun subscribeToChannelFromBackground(channel: String)
    //TODO: Revisar si se puede usar el metodo status connect o status channel
    fun getPusherChannel(channel: String): Boolean
    fun renderRemoteVideo()
    fun createAnswer()
    fun startWebRTCService(callModel: CallModel)

}