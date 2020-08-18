package com.naposystems.napoleonchat.ui.blockedContacts

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.entity.Contact

interface IContractBlockedContact {

    interface ViewModel {
        fun getBlockedContacts()
        fun searchLocalBlockedContact(query: String)
    }

    interface Repository {
        suspend fun getRemoteBlockedContacts()
        suspend fun getBlockedContactsLocal(): LiveData<List<Contact>>
    }
}