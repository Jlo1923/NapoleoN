package com.naposystems.pepito.service.webRTCCall

interface IContractWebRTCCallService {

    interface Repository {
        fun rejectCall(contactId: Int, channel: String)
    }
}