package com.naposystems.napoleonchat.service.webRTCCall

interface IContractWebRTCCallService {

    interface Repository {
        fun rejectCall(contactId: Int, channel: String)
    }
}