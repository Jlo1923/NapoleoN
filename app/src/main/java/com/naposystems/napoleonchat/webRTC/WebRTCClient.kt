package com.naposystems.napoleonchat.webRTC

import android.widget.TextView
import com.pusher.client.channel.PrivateChannel
import org.webrtc.SurfaceViewRenderer

interface WebRTCClient {

    var contactId: Int

    fun setWebRTCClientListener(webRTCClientListener: WebRTCClientListener)

    //    fun getContactId(): Int
//    fun setContactId(contactId: Int)
    fun isVideoCall(): Boolean
    fun setIsVideoCall(isVideoCall: Boolean)
    fun getTypeCall(): Int
    fun setTypeCall(typeCall: Int)
    fun getChannel(): String
    fun setChannel(channel: String)
    fun setOffer(offer: String?)
    fun subscribeToCallChannel(isActionAnswer: Boolean)
    fun setTextViewCallDuration(textView: TextView)
    fun setLocalVideoView(surfaceViewRenderer: SurfaceViewRenderer)
    fun setRemoteVideoView(surfaceViewRenderer: SurfaceViewRenderer)
    fun setSpeakerOn(isChecked: Boolean)
    fun setMicOff()
    fun setItsReturnCall(itsReturnCall: Boolean)
    fun getMicIsOn(): Boolean
    fun isSpeakerOn(): Boolean
    fun isVideoMuted(): Boolean
    fun isBluetoothActive(): Boolean
    fun contactTurnOffCamera(): Boolean
    fun setIsOnCallActivity(isOnCallActivity: Boolean)
    fun initSurfaceRenders()
    fun startCaptureVideo()
    fun emitJoinToCall()
    fun stopRingAndVibrate()
    fun emitHangUp()
    fun changeToVideoCall()
    fun cancelChangeToVideoCall()
    fun muteVideo(checked: Boolean, itsFromBackPressed: Boolean = false)
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
    fun subscribeToChannelFromBackground(channel: String)
    fun getPusherChannel(channel: String): PrivateChannel?
    fun renderRemoteVideo()
    fun createAnswer()
}