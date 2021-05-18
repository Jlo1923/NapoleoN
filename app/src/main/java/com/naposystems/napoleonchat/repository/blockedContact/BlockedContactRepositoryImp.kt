package com.naposystems.napoleonchat.repository.blockedContact

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.source.local.datasource.contact.ContactLocalDataSource
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import javax.inject.Inject

class BlockedContactRepositoryImp
@Inject constructor(
    private val contactLocalDataSource: ContactLocalDataSource
) : BlockedContactRepository {

    override suspend fun getBlockedContactsLocal(): LiveData<List<ContactEntity>> {
        return contactLocalDataSource.getBlockedContacts()
    }

}