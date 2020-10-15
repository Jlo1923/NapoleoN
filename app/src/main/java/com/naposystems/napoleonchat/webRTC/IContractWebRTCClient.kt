package com.naposystems.napoleonchat.webRTC

import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.webService.socket.IContractSocketService
import com.pusher.client.channel.PrivateChannel
import org.webrtc.SurfaceViewRenderer

interface IContractWebRTCClient {
    fun setListener(webRTCClientListener: WebRTCClient.WebRTCClientListener)
    fun getContactId(): Int
    fun setContactId(contactId: Int)
    fun isVideoCall(): Boolean
    fun setIsVideoCall(isVideoCall: Boolean)
    fun isIncomingCall(): Boolean
    fun setIncomingCall(incomingCall: Boolean)
    fun getChannel(): String
    fun setChannel(channel: String)
    fun subscribeToChannel(isActionAnswer: Boolean)
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
}