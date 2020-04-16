package com.naposystems.pepito.ui.contacts

import androidx.lifecycle.LiveData
import com.naposystems.pepito.entity.Contact

interface IContractContacts {

    interface ViewModel {
        fun getLocalContacts()
        fun resetContactsLoaded()
        fun searchContact(query: String)
    }

    interface Repository {
        suspend fun getLocalContacts(): LiveData<List<Contact>>
        suspend fun getLocalContactsForSearch(): LiveData<List<Contact>>
    }
}