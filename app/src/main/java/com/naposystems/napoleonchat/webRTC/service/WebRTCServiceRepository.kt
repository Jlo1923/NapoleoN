package com.naposystems.napoleonchat.webRTC.service

interface WebRTCServiceRepository {
    fun rejectCall(contactId: Int, channel: String)
}