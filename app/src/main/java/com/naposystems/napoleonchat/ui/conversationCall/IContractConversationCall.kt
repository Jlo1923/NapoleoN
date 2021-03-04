package com.naposystems.napoleonchat.ui.conversationCall

import com.naposystems.napoleonchat.source.remote.dto.cancelCall.CancelCallReqDTO
import com.naposystems.napoleonchat.source.remote.dto.cancelCall.CancelCallResDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.message.MessageReqDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.message.MessageResDTO
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import retrofit2.Response

interface IContractConversationCall {

    interface ViewModel {
        fun getContact(contactId: Int)
        fun resetIsOnCallPref()
        fun sendMissedCall(contactId: Int, isVideoCall: Boolean)
        fun cancelCall(contactId: Int, channel: String)
    }

    interface Repository {
        suspend fun getContactById(contactId: Int): ContactEntity?
        fun resetIsOnCallPref()
        suspend fun sendMissedCall(messageReqDTO: MessageReqDTO): Response<MessageResDTO>
        fun getUserDisplayFormat(): Int
        suspend fun cancelCall(cancelCallReqDTO: CancelCallReqDTO): Response<CancelCallResDTO>
    }
}