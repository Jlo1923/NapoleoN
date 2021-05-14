package com.naposystems.napoleonchat.repository.conversationCall

import com.naposystems.napoleonchat.source.local.datasource.contact.ContactLocalDataSource
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.source.remote.api.NapoleonApi
import com.naposystems.napoleonchat.source.remote.dto.cancelCall.CancelCallReqDTO
import com.naposystems.napoleonchat.source.remote.dto.cancelCall.CancelCallResDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.call.reject.RejectCallReqDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.message.MessageReqDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.message.MessageResDTO
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import retrofit2.Response
import javax.inject.Inject

class ConversationCallRepositoryImp @Inject constructor(
    private val napoleonApi: NapoleonApi,
    private val contactLocalDataSource: ContactLocalDataSource,
    private val sharedPreferencesManager: SharedPreferencesManager
) : ConversationCallRepository {

    override suspend fun getContactById(contactId: Int): ContactEntity? {
        return contactLocalDataSource.getContactById(contactId)
    }

    override suspend fun sendMissedCall(messageReqDTO: MessageReqDTO): Response<MessageResDTO> {
        return napoleonApi.sendMessage(messageReqDTO)
    }

    override fun getUserDisplayFormat() =
        sharedPreferencesManager.getInt(Constants.SharedPreferences.PREF_USER_DISPLAY_FORMAT)

    override suspend fun cancelCall(cancelCallReqDTO: CancelCallReqDTO): Response<CancelCallResDTO> {
        return napoleonApi.cancelCall(cancelCallReqDTO)
    }

    override suspend fun rejectCall(rejectCallReqDTO: RejectCallReqDTO) {
        napoleonApi.rejectCall(rejectCallReqDTO)
    }
}