package com.naposystems.napoleonchat.service.socketMessage

import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

interface SocketEventsListener {

    interface Conection {

    }

    interface Message {

    }

    interface Call {

        //region Conection
        fun itsSubscribedToCallChannel(
            contactId: Int,
            channelName: String,
            isVideoCall: Boolean
        )

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


