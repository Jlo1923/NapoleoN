package com.naposystems.napoleonchat.webRTC.service

import com.naposystems.napoleonchat.source.remote.api.NapoleonApi
import com.naposystems.napoleonchat.utility.TypeEndCallEnum
import com.naposystems.napoleonchat.webRTC.client.WebRTCClient
import timber.log.Timber
import javax.inject.Inject

class WebRTCServiceRepositoryImp
@Inject constructor(
    private val napoleonApi: NapoleonApi,
    private val webRTCClient: WebRTCClient,
) : WebRTCServiceRepository {

    override fun disposeCall(typeEndCall: TypeEndCallEnum?) {
        Timber.d("LLAMADA PASO: WEBRTCSERVICE")
        if (typeEndCall == null) {
            webRTCClient.disposeCall()
        } else {
            webRTCClient.disposeCall(typeEndCall)
        }

    }
}