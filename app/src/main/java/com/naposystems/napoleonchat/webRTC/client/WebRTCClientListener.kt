package com.naposystems.napoleonchat.webRTC.client

interface WebRTCClientListener {

    //Turn ON/OFF Camera
    fun toggleContactCamera(isVisible: Boolean)

    //Change to videocall
    fun contactWantChangeToVideoCall()
    fun contactAcceptChangeToVideoCall()
    fun contactCancelChangeToVideoCall()
    fun contactCantChangeToVideoCall()

    //UI
    fun showTimer()
    fun showConnectingTitle()
    fun enableControls()
    fun showRemoteVideo()
    fun changeTextviewTitle(stringResourceId: Int)
    fun toggleLocalRenderVisibility(visibility: Int)
    fun toggleBluetoothButtonVisibility(isVisible: Boolean)
    fun toggleCheckedSpeaker(checked: Boolean)

    fun hangupByNotification()
    fun onContactNotAnswer()
    fun callEnded()

}