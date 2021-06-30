package com.naposystems.napoleonchat.webRTC.service

import com.naposystems.napoleonchat.utility.TypeEndCallEnum
import com.naposystems.napoleonchat.webRTC.client.WebRTCClient
import timber.log.Timber
import javax.inject.Inject

class WebRTCServiceRepositoryImp
@Inject constructor(
    private val webRTCClient: WebRTCClient,
) : WebRTCServiceRepository {

    override fun disposeCall(typeEndCall: TypeEndCallEnum?) {
        Timber.d("LLAMADA PASO: WEBRTCSERVICE")
        if (typeEndCall == null) {
            webRTCClient.emitHangUp()
            webRTCClient.playEndCall()
        } else {
            webRTCClient.disposeCall(typeEndCall)
        }
    }

    override fun contactCancelCall() {
        webRTCClient.contactCancelCall()
    }

    override fun contactRejectCall() {
        webRTCClient.contactRejectCall()
    }
}