package com.naposystems.napoleonchat.repository.blockedContact

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.source.local.datasource.contact.ContactLocalDataSource
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.ui.blockedContacts.IContractBlockedContact
import javax.inject.Inject

class BlockedContactRepository
@Inject constructor(
    private val contactLocalDataSource: ContactLocalDataSource
) : IContractBlockedContact.Repository {

    override suspend fun getBlockedContactsLocal(): LiveData<List<ContactEntity>> {
        return contactLocalDataSource.getBlockedContacts()
    }

}