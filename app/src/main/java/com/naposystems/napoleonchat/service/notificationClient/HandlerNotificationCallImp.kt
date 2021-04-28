package com.naposystems.napoleonchat.service.notificationClient

import com.naposystems.napoleonchat.model.CallModel
import com.naposystems.napoleonchat.webRTC.client.WebRTCClient
import timber.log.Timber
import javax.inject.Inject

class HandlerNotificationCallImp
@Inject constructor(
    private val webRTCClient: WebRTCClient,
) : HandlerNotificationCall {

    override fun handlerCall(callModel: CallModel) {

        Timber.d("LLAMADA PASO 2: EN HANDLER CALL $callModel")

        webRTCClient.connectSocket(
            mustSubscribeToPresenceChannel = true,
            callModel = callModel
        )

    }

}
