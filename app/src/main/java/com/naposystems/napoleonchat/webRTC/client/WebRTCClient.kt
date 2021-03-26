package com.naposystems.napoleonchat.webRTC.client

import android.widget.TextView
import com.pusher.client.channel.PrivateChannel
import org.webrtc.SurfaceViewRenderer

interface WebRTCClient {

    var isActiveCall: Boolean
    var contactId: Int
    var isVideoCall: Boolean
    var typeCall: Int
    var channel: String

    fun setWebRTCClientListener(webRTCClientListener: WebRTCClientListener)
    fun subscribeToCallChannel()
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
    fun setIsOnCallActivity(isOnCallActivity: Boolean)
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
    fun unSubscribeCallChannel()
//    fun subscribeToChannelFromBackground(channel: String)
    //TODO: Revisar si se puede usar el metodo status connect o status channel
    fun getPusherChannel(channel: String): PrivateChannel?
    fun renderRemoteVideo()
    fun createAnswer()

}