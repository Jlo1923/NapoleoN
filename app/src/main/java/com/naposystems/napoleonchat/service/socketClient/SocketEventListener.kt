package com.naposystems.napoleonchat.service.socketClient

import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

interface SocketEventListener {

    fun itsSubscribedToPresenceChannelIncomingCall()
    fun itsSubscribedToPresenceChannelOutgoingCall()

    //region Conection
    fun iceCandidateReceived(iceCandidate: IceCandidate)
    fun offerReceived(sessionDescription: SessionDescription)
    fun answerReceived(sessionDescription: SessionDescription)
    //endregion

    //region Handler Call
    fun contactRejectCall()
    fun contactCancelCall()
    //endregion

    //Contact change to video call
    fun contactWantChangeToVideoCall()
    fun contactAcceptChangeToVideoCall()
    fun contactCancelChangeToVideoCall()
    fun contactCantChangeToVideoCall()

    fun toggleContactCamera(isVisible: Boolean)

    fun contactHasHangup()
    //endregion

    fun processDisposeCall()

}


