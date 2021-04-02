package com.naposystems.napoleonchat.service.notificationClient

import com.naposystems.napoleonchat.model.CallModel
import com.naposystems.napoleonchat.service.socketClient.SocketClient
import timber.log.Timber
import javax.inject.Inject

class HandlerNotificationCallImp
@Inject constructor(
    private val socketClient: SocketClient,
) : HandlerNotificationCall {

    override fun handlerCall(callModel: CallModel) {

        Timber.d("LLAMADA PASO 3: EN HANDLER CALL $callModel")

        socketClient.connectSocket(
            mustSubscribeToPresenceChannel = true,
            callModel = callModel
        )

    }

}
