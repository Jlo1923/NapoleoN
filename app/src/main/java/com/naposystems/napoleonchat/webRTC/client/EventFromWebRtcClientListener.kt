package com.naposystems.napoleonchat.webRTC.client

interface EventFromWebRtcClientListener {

    //Turn ON/OFF Camera
    fun toggleContactCamera(visibility: Int)

    //Change to videocall
    fun contactWantChangeToVideoCall()
    fun contactAcceptChangeToVideoCall()
    fun contactCancelChangeToVideoCall()
    fun contactCantChangeToVideoCall()

    //UI
    fun showTimer()
    fun showCypheryngCall()
    fun showReConnectingCall()
    fun showOccupiedCall()
    fun showFinishingCall()
    fun enableControls()
    fun handlerActiveCall()
    fun showRemoteVideo()
    fun showTypeCallTitle()
    fun toggleLocalRenderVisibility(visibility: Boolean  = false)
    fun toggleBluetoothButtonVisibility(isVisible: Boolean)
    fun toggleCheckedSpeaker(checked: Boolean)

    fun hangUpFromNotification()
    fun onContactNotAnswer()
    fun callEnded()

}