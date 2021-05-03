package com.naposystems.napoleonchat.webRTC.service

import com.naposystems.napoleonchat.model.CallModel
import com.naposystems.napoleonchat.utility.TypeEndCallEnum

interface WebRTCServiceRepository {
    fun rejectCall()
    fun disposeCall(typeEndCall: TypeEndCallEnum? = null)
    fun sendMissedCall()
    fun cancelCall()
}