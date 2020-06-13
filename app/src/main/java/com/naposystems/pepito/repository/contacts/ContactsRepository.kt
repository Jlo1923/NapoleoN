package com.naposystems.pepito.repository.contacts

import androidx.lifecycle.LiveData
import com.naposystems.pepito.db.dao.contact.ContactDataSource
import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.ui.contacts.IContractContacts
import javax.inject.Inject

class ContactsRepository @Inject constructor(
    private val contactLocalDataSource: ContactDataSource
) :
    IContractContacts.Repository {

    override suspend fun getLocalContacts(): LiveData<MutableList<Contact>> {
        return contactLocalDataSource.getContacts()
    }

    override suspend fun getLocalContactsForSearch(): LiveData<MutableList<Contact>> {
        return contactLocalDataSource.getContacts()
    }
}