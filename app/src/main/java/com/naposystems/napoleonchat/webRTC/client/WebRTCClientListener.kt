package com.naposystems.napoleonchat.webRTC.client

interface WebRTCClientListener {

    //Turn ON/OFF Camera
    fun toggleContactCamera(visibility: Int)

    //Change to videocall
    fun contactWantChangeToVideoCall()
    fun contactAcceptChangeToVideoCall()
    fun contactCancelChangeToVideoCall()
    fun contactCantChangeToVideoCall()

    //UI
    fun showTimer()
    fun showConnectingTitle()
    fun showReConnectingTitle()
    fun enableControls()
    fun showRemoteVideo()
    fun changeTextviewTitle(stringResourceId: Int)
    fun toggleLocalRenderVisibility(visibility: Int)
    fun toggleBluetoothButtonVisibility(isVisible: Boolean)
    fun toggleCheckedSpeaker(checked: Boolean)

    fun hangUpFromNotification()
    fun onContactNotAnswer()
    fun callEnded()

}