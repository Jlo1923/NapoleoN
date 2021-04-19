package com.naposystems.napoleonchat.webRTC.service

import com.naposystems.napoleonchat.model.CallModel
import com.naposystems.napoleonchat.source.remote.api.NapoleonApi
import com.naposystems.napoleonchat.source.remote.dto.conversation.call.reject.RejectCallReqDTO
import com.naposystems.napoleonchat.webRTC.client.WebRTCClient
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class WebRTCServiceRepositoryImp
@Inject constructor(
    private val napoleonApi: NapoleonApi,
    private val webRTCClient: WebRTCClient,
) : WebRTCServiceRepository {

    override fun rejectCall(callModel: CallModel) {
        GlobalScope.launch {
            val rejectCallReqDTO = RejectCallReqDTO(
                contactId = callModel.contactId,
                channel = callModel.channelName
            )
            val response = napoleonApi.rejectCall(rejectCallReqDTO)

            if (response.isSuccessful) {
                Timber.d("LLamada rechazada bb")
            }
        }
    }

    override fun disposeCall(callModel: CallModel) {
        webRTCClient.disposeCall(callModel)
    }
}