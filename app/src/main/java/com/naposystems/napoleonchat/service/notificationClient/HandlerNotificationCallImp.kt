package com.naposystems.napoleonchat.service.notificationClient

import com.naposystems.napoleonchat.app.NapoleonApplication
import com.naposystems.napoleonchat.utility.isNoCall
import com.naposystems.napoleonchat.webRTC.client.WebRTCClient
import timber.log.Timber
import javax.inject.Inject

class HandlerNotificationCallImp
@Inject constructor(
    private val webRTCClient: WebRTCClient,
) : HandlerNotificationCall {

    override fun handlerCall() {

        Timber.d("LLAMADA PASO 2: EN HANDLER CALL")
        if (NapoleonApplication.statusCall.isNoCall()) {
            webRTCClient.connectSocket()
        } else {
            NapoleonApplication.callModel?.let {
                webRTCClient.rejectCall()
            }
        }
    }

}
