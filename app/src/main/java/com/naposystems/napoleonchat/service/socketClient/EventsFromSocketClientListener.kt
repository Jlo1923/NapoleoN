package com.naposystems.napoleonchat.service.socketClient

import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

interface EventsFromSocketClientListener {

    fun itsSubscribedToPresenceChannelIncomingCall()
    fun itsSubscribedToPresenceChannelOutgoingCall()

    //Conection
    fun iceCandidateReceived(iceCandidate: IceCandidate)
    fun offerReceived(sessionDescription: SessionDescription)
    fun answerReceived(sessionDescription: SessionDescription)

    //Handler Call
    fun listenerRejectCall()
    fun listenerCancelCall()

    //VideoCall
    fun contactWantChangeToVideoCall()
    fun contactAcceptChangeToVideoCall()
    fun contactCancelChangeToVideoCall()
    fun contactCantChangeToVideoCall()

    //Turn ON/OFF Camera
    fun toggleContactCamera(isVisible: Boolean)

    fun contactHasHangup()

    fun processDisposeCall()

}


