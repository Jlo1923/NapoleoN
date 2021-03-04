package com.naposystems.napoleonchat.ui.contacts

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.source.local.entity.ContactEntity

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
        suspend fun getLocalContacts(): LiveData<MutableList<ContactEntity>>
        suspend fun getLocalContactsForSearch(): LiveData<MutableList<ContactEntity>>
    }
}