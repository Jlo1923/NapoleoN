package com.naposystems.napoleonchat.repository.contacts

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.source.local.datasource.contact.ContactLocalDataSource
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.ui.contacts.IContractContacts
import javax.inject.Inject

class ContactsRepository @Inject constructor(
    private val contactLocalDataSource: ContactLocalDataSource
) :
    IContractContacts.Repository {

    override suspend fun getLocalContacts(): LiveData<MutableList<ContactEntity>> {
        return contactLocalDataSource.getContacts()
    }

    override suspend fun getLocalContactsForSearch(): LiveData<MutableList<ContactEntity>> {
        return contactLocalDataSource.getContacts()
    }
}