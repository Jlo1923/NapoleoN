package com.naposystems.napoleonchat.ui.blockedContacts

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.source.local.entity.ContactEntity

interface IContractBlockedContact {

    interface ViewModel {
        fun getBlockedContacts()
        fun searchLocalBlockedContact(query: String)
    }

    interface Repository {
        suspend fun getBlockedContactsLocal(): LiveData<List<ContactEntity>>
    }
}