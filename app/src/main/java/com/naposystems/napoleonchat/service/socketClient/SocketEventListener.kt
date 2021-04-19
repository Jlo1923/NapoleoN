package com.naposystems.napoleonchat.service.socketClient

import com.naposystems.napoleonchat.model.CallModel
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

interface SocketEventListener {

    fun itsSubscribedToPresenceChannelIncomingCall(callModel: CallModel)
    fun itsSubscribedToPresenceChannelOutgoingCall(callModel: CallModel)

    //region Conection
    fun iceCandidateReceived(channelName: String, iceCandidate: IceCandidate)
    fun offerReceived(channelName: String, sessionDescription: SessionDescription)
    fun answerReceived(channelName: String, sessionDescription: SessionDescription)
    //endregion

    //region Handler Call
    fun contactRejectCall(channelName: String)
    fun contactCancelCall(channelName: String)
    //endregion

    //Contact change to video call
    fun contactWantChangeToVideoCall(channelName: String)
    fun contactAcceptChangeToVideoCall(channelName: String)
    fun contactCancelChangeToVideoCall(channelName: String)
    fun contactCantChangeToVideoCall(channelName: String)

    fun toggleContactCamera(channelName: String, isVisible: Boolean)

    fun contactHasHangup(channelName: String)
    //endregion


}


