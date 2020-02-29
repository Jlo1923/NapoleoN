package com.naposystems.pepito.ui.contacts

import androidx.lifecycle.LiveData
import com.naposystems.pepito.dto.contacts.blockedContact.BlockedContactResDTO
import com.naposystems.pepito.dto.contacts.deleteContact.DeleteContactResDTO
import com.naposystems.pepito.entity.Contact
import retrofit2.Response

interface IContractContacts {

    interface ViewModel {
        fun getContacts()
        fun resetContactsLoaded()
        fun searchContact(query: String)
    }

    interface Repository {
        suspend fun getLocalContacts(): LiveData<List<Contact>>
        suspend fun getLocalContactsForSearch(): LiveData<List<Contact>>
        suspend fun getRemoteContacts()
    }
}