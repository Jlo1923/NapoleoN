package com.naposystems.napoleonchat.repository.conversationCall

import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.source.remote.dto.cancelCall.CancelCallReqDTO
import com.naposystems.napoleonchat.source.remote.dto.cancelCall.CancelCallResDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.call.reject.RejectCallReqDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.message.MessageReqDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.message.MessageResDTO
import retrofit2.Response

interface ConversationCallRepository {
    suspend fun getContactById(contactId: Int): ContactEntity?
    suspend fun sendMissedCall(messageReqDTO: MessageReqDTO): Response<MessageResDTO>
    fun getUserDisplayFormat(): Int
    suspend fun cancelCall(cancelCallReqDTO: CancelCallReqDTO): Response<CancelCallResDTO>
    suspend fun rejectCall(rejectCallReqDTO: RejectCallReqDTO)
}