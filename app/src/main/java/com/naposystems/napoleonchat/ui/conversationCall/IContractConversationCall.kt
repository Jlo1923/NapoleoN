package com.naposystems.napoleonchat.ui.conversationCall

import com.naposystems.napoleonchat.dto.conversation.message.MessageReqDTO
import com.naposystems.napoleonchat.dto.conversation.message.MessageResDTO
import com.naposystems.napoleonchat.entity.Contact
import retrofit2.Response

interface IContractConversationCall {

    interface ViewModel {
        fun getContact(contactId: Int)
        fun resetIsOnCallPref()
        fun sendMissedCall(contactId: Int, isVideoCall: Boolean)
    }

    interface Repository {
        suspend fun getContactById(contactId: Int): Contact?
        fun resetIsOnCallPref()
        suspend fun sendMissedCall(messageReqDTO: MessageReqDTO): Response<MessageResDTO>
        fun getUserDisplayFormat(): Int
    }
}