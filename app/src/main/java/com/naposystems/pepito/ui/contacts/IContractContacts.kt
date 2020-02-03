package com.naposystems.pepito.ui.contacts

import androidx.lifecycle.LiveData
import com.naposystems.pepito.entity.Contact

interface IContractContacts {

    interface ViewModel {
        fun getContacts()
    }

    interface Repository {
        suspend fun getLocalContacts(): LiveData<List<Contact>>
        suspend fun getRemoteContacts()
    }
}