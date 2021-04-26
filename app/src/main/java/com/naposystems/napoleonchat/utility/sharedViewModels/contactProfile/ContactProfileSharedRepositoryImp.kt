package com.naposystems.napoleonchat.utility.sharedViewModels.contactProfile

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.source.local.datasource.contact.ContactLocalDataSource
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import javax.inject.Inject

class ContactProfileSharedRepositoryImp
@Inject constructor(
    private val contactLocalDataSource: ContactLocalDataSource
) : ContactProfileSharedRepository {

    override fun getLocalContact(contactId: Int): LiveData<ContactEntity> {
        return contactLocalDataSource.getContact(contactId)
    }
}