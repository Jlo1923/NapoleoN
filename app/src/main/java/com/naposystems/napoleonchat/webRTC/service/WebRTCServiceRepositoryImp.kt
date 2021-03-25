package com.naposystems.napoleonchat.webRTC.service

import com.naposystems.napoleonchat.source.remote.api.NapoleonApi
import com.naposystems.napoleonchat.source.remote.dto.conversation.call.reject.RejectCallReqDTO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class WebRTCServiceRepositoryImp
@Inject constructor(
    private val napoleonApi: NapoleonApi
) : WebRTCServiceRepository {

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