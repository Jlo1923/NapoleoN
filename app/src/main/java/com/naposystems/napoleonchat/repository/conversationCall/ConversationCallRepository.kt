package com.naposystems.napoleonchat.repository.conversationCall

import com.naposystems.napoleonchat.db.dao.contact.ContactDataSource
import com.naposystems.napoleonchat.dto.cancelCall.CancelCallReqDTO
import com.naposystems.napoleonchat.dto.cancelCall.CancelCallResDTO
import com.naposystems.napoleonchat.dto.conversation.message.MessageReqDTO
import com.naposystems.napoleonchat.dto.conversation.message.MessageResDTO
import com.naposystems.napoleonchat.entity.Contact
import com.naposystems.napoleonchat.ui.conversationCall.IContractConversationCall
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.webService.NapoleonApi
import retrofit2.Response
import javax.inject.Inject

class ConversationCallRepository @Inject constructor(
    private val napoleonApi: NapoleonApi,
    private val contactLocalDataSource: ContactDataSource,
    private val sharedPreferencesManager: SharedPreferencesManager
) : IContractConversationCall.Repository {

    override suspend fun getContactById(contactId: Int): Contact? {
        return contactLocalDataSource.getContactById(contactId)
    }

    override fun resetIsOnCallPref() {
        sharedPreferencesManager.putBoolean(Constants.SharedPreferences.PREF_IS_ON_CALL, false)
    }

    override suspend fun sendMissedCall(messageReqDTO: MessageReqDTO): Response<MessageResDTO> {
        return napoleonApi.sendMessage(messageReqDTO)
    }

    override fun getUserDisplayFormat() =
        sharedPreferencesManager.getInt(Constants.SharedPreferences.PREF_USER_DISPLAY_FORMAT)

    override suspend fun cancelCall(cancelCallReqDTO: CancelCallReqDTO): Response<CancelCallResDTO> {
        return napoleonApi.cancelCall(cancelCallReqDTO)
    }
}