package com.naposystems.napoleonchat.webRTC.service

import com.naposystems.napoleonchat.model.CallModel
import com.naposystems.napoleonchat.source.remote.api.NapoleonApi
import com.naposystems.napoleonchat.source.remote.dto.cancelCall.CancelCallReqDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.call.reject.RejectCallReqDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.message.MessageReqDTO
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.webRTC.client.WebRTCClient
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
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

            napoleonApi.rejectCall(rejectCallReqDTO)

        }
    }

    override fun cancelCall(callModel: CallModel) {
        GlobalScope.launch {
            val cancelCallReqDTO = CancelCallReqDTO(
                contactId = callModel.contactId,
                channel = callModel.channelName
            )

            Timber.d("LLAMADA PASO: LLAMADA COLGADA DESDE EL SERVICIO")

            napoleonApi.cancelCall(cancelCallReqDTO)

        }
    }


    override fun sendMissedCall(callModel: CallModel) {
        GlobalScope.launch {
            try {

                val messageReqDTO = MessageReqDTO(
                    userDestination = callModel.contactId,
                    quoted = "",
                    body = "",
                    numberAttachments = 0,
                    destroy = Constants.SelfDestructTime.EVERY_ONE_DAY.time,
                    messageType = if (callModel.isVideoCall) Constants.MessageTextType.MISSED_VIDEO_CALL.type else Constants.MessageTextType.MISSED_CALL.type,
                    uuidSender = UUID.randomUUID().toString()
                )

                napoleonApi.sendMessage(messageReqDTO)

            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }


    override fun disposeCall(callModel: CallModel) {
        Timber.d("LLAMADA PASO: WEBRTCSERVICE")
        webRTCClient.disposeCall(callModel)
    }
}