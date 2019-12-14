package com.naposystems.pepito.ui.contacts

import com.naposystems.pepito.entity.Contact

interface IContractContacts {

    interface ViewModel {
        fun getContacts()
    }

    interface Repository {
        suspend fun getContacts(): List<Contact>
    }
}