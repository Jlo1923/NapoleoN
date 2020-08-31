package com.naposystems.napoleonchat.ui.contacts

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.entity.Contact

interface IContractContacts {

    interface ViewModel {
        fun getLocalContacts()
        fun resetContactsLoaded()
        fun searchContact(query: String)
        fun setTextSearch(text : String)
        fun getTextSearch() : String
        fun resetTextSearch()
    }

    interface Repository {
        suspend fun getLocalContacts(): LiveData<MutableList<Contact>>
        suspend fun getLocalContactsForSearch(): LiveData<MutableList<Contact>>
    }
}