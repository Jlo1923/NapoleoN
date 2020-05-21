package com.naposystems.pepito.repository.webRTCCallService

import com.naposystems.pepito.dto.conversation.call.reject.RejectCallReqDTO
import com.naposystems.pepito.service.webRTCCall.IContractWebRTCCallService
import com.naposystems.pepito.webService.NapoleonApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class WebRTCCallServiceRepository @Inject constructor(private val napoleonApi: NapoleonApi) :
    IContractWebRTCCallService.Repository {

    override fun rejectCall(contactId: Int, channel: String) {
        GlobalScope.launch {
            val rejectCallReqDTO = RejectCallReqDTO(
                contactId = contactId,
                channel = channel
            )
            val response = napoleonApi.rejectCall(rejectCallReqDTO)

            if (response.isSuccessful) {
                Timber.d("LLamada rechazada bb")
            }
        }
    }
}