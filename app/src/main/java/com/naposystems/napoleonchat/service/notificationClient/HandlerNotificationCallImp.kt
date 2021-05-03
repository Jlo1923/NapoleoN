package com.naposystems.napoleonchat.service.notificationClient

import com.naposystems.napoleonchat.app.NapoleonApplication
import com.naposystems.napoleonchat.model.CallModel
import com.naposystems.napoleonchat.model.toCallModel
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.isNoCall
import com.naposystems.napoleonchat.webRTC.client.WebRTCClient
import timber.log.Timber
import javax.inject.Inject

class HandlerNotificationCallImp
@Inject constructor(
    private val webRTCClient: WebRTCClient,
) : HandlerNotificationCall {

    override fun handlerCall(dataFromNotification: Map<String, String>) {
        Timber.d("LLAMADA PASO 2: EN HANDLER CALL")
        if (NapoleonApplication.statusCall.isNoCall()) {

            Timber.d("LLAMADA PASO 1: APLICACION NO VISIBLE")

            NapoleonApplication.callModel = dataFromNotification.toCallModel()

            NapoleonApplication.callModel?.let {
                it.typeCall = Constants.TypeCall.IS_INCOMING_CALL
            }

            NapoleonApplication.callModel?.let {
                it.mustSubscribeToPresenceChannel = true
            }

            if (NapoleonApplication.isVisible.not()) {
                NapoleonApplication.callModel?.let {
                    it.isFromClosedApp = Constants.FromClosedApp.YES
                }
            }

            webRTCClient.connectSocket()
        } else {
            NapoleonApplication.callModel?.let {

                val callModel: CallModel = dataFromNotification.toCallModel()

                webRTCClient.rejectSecondCall(
                    contactId = callModel.contactId,
                    channelName = callModel.channelName
                )
            }
        }
    }

}
