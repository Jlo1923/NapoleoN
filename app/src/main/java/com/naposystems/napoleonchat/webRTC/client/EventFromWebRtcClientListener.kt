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
    fun showReConnectingTitle()
    fun showOccupiedTitle()
    fun showFinishingTitle()
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