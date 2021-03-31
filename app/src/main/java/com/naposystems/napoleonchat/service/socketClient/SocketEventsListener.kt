package com.naposystems.napoleonchat.service.socketClient

import com.naposystems.napoleonchat.model.CallModel
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

interface SocketEventsListener {

    interface Conection {

    }

    interface Message {

    }

    interface CallOutApp{
        fun itsSubscribedToPresenceChannelIncomingCall(callModel: CallModel)
    }

    interface Call {
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

        //region ChangeToVideoCall
        fun contactWantChangeToVideoCall(channelName: String)
        fun contactAcceptChangeToVideoCall(channelName: String)
        fun contactCancelChangeToVideoCall(channelName: String)
        fun contactCantChangeToVideoCall(channelName: String)
        //endregion

        //region Handler Camera
        fun contactTurnOnCamera(channelName: String)
        fun contactTurnOffCamera(channelName: String)
        //endregion

        //region Hangup
        fun contactHasHangup(channelName: String)

        //endregion
    }

}


