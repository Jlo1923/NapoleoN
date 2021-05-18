package com.naposystems.napoleonchat.webRTC.service

import com.naposystems.napoleonchat.utility.TypeEndCallEnum

interface WebRTCServiceRepository {
    fun disposeCall(typeEndCall: TypeEndCallEnum? = null)
}