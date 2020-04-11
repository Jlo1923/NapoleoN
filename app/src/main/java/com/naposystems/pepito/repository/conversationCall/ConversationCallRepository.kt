package com.naposystems.pepito.repository.conversationCall

import com.naposystems.pepito.db.dao.contact.ContactDataSource
import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.ui.conversationCall.IContractConversationCall
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.SharedPreferencesManager
import javax.inject.Inject

class ConversationCallRepository @Inject constructor(
    private val contactLocalDataSource: ContactDataSource,
    private val sharedPreferencesManager: SharedPreferencesManager
) : IContractConversationCall.Repository {

    override suspend fun getContactById(contactId: Int): Contact? {
        return contactLocalDataSource.getContactById(contactId)
    }

    override fun resetIsOnCallPref() {
        sharedPreferencesManager.putBoolean(Constants.SharedPreferences.PREF_IS_ON_CALL, false)
    }
}