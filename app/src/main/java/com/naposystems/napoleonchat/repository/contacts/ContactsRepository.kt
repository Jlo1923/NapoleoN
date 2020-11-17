package com.naposystems.napoleonchat.repository.contacts

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.db.dao.contact.ContactDataSource
import com.naposystems.napoleonchat.entity.Contact
import com.naposystems.napoleonchat.ui.contacts.IContractContacts
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