package com.naposystems.napoleonchat.repository.blockedContact

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.db.dao.contact.ContactDataSource
import com.naposystems.napoleonchat.entity.Contact
import com.naposystems.napoleonchat.ui.blockedContacts.IContractBlockedContact
import javax.inject.Inject

class BlockedContactRepository
@Inject constructor(
    private val contactLocalDataSource: ContactDataSource
) : IContractBlockedContact.Repository {

    override suspend fun getBlockedContactsLocal(): LiveData<List<Contact>> {
        return contactLocalDataSource.getBlockedContacts()
    }

}