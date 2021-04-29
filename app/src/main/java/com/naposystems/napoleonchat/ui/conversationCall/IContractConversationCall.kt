package com.naposystems.napoleonchat.ui.conversationCall

import com.naposystems.napoleonchat.model.CallModel
import com.naposystems.napoleonchat.source.remote.dto.cancelCall.CancelCallReqDTO
import com.naposystems.napoleonchat.source.remote.dto.cancelCall.CancelCallResDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.message.MessageReqDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.message.MessageResDTO
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.source.remote.dto.conversation.call.reject.RejectCallReqDTO
import retrofit2.Response

interface IContractConversationCall {

    interface ViewModel {
        fun getContact(contactId: Int)
        fun sendMissedCall(callModel: CallModel)
        fun cancelCall(callModel: CallModel)
        fun rejectCall(callModel: CallModel)
    }

    interface Repository {
        suspend fun getContactById(contactId: Int): ContactEntity?
        suspend fun sendMissedCall(messageReqDTO: MessageReqDTO): Response<MessageResDTO>
        fun getUserDisplayFormat(): Int
        suspend fun cancelCall(cancelCallReqDTO: CancelCallReqDTO): Response<CancelCallResDTO>
        suspend fun rejectCall(rejectCallReqDTO: RejectCallReqDTO)
    }
}