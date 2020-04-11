package com.naposystems.pepito.reactive

import org.json.JSONObject
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

class RxEvent {

    data class NewMessageReceivedEvent(val channelName: String, val jsonObject: JSONObject)
    class NewFriendshipRequest
    class NoInternetConnection
    class AccountAttack
    data class IncomingCall(val channel: String, val contactId: Int, val isVideoCall: Boolean)
    data class ContactHasJoinToCall(val channel: String)
    data class IceCandidateReceived(val channel: String, val iceCandidate: IceCandidate)
    data class OfferReceived(val channel: String, val sessionDescription: SessionDescription)
    data class AnswerReceived(val channel: String, val sessionDescription: SessionDescription)
    data class ContactHasHangup(val channel: String)
    data class ContactWantChangeToVideoCall(val channel: String)
    data class ContactAcceptChangeToVideoCall(val channel: String)
    data class ContactTurnOffCamera(val channel: String)
    data class ContactTurnOnCamera(val channel: String)
    data class ContactRejectCall(val channel: String)
}