package com.naposystems.pepito.ui.conversationCall

import com.naposystems.pepito.entity.Contact

interface IContractConversationCall {

    interface ViewModel {
        fun getContact(contactId: Int)
        fun resetIsOnCallPref()
    }

    interface Repository {
        suspend fun getContactById(contactId: Int): Contact?
        fun resetIsOnCallPref()
    }
}