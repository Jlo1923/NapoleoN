package com.naposystems.napoleonchat.webRTC.service

import com.naposystems.napoleonchat.model.CallModel

interface WebRTCServiceRepository {
    fun rejectCall(callModel: CallModel)
}