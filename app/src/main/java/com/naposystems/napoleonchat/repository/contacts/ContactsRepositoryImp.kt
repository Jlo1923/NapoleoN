package com.naposystems.napoleonchat.repository.contacts

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.source.local.datasource.contact.ContactLocalDataSource
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import javax.inject.Inject

class ContactsRepositoryImp
@Inject constructor(
    private val contactLocalDataSource: ContactLocalDataSource
) : ContactsRepository {

    override suspend fun getLocalContacts(): LiveData<MutableList<ContactEntity>> {
        return contactLocalDataSource.getContacts()
    }

    override suspend fun getLocalContactsForSearch(): LiveData<MutableList<ContactEntity>> {
        return contactLocalDataSource.getContacts()
    }
}