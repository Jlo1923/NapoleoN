package com.naposystems.napoleonchat.repository.sharedRepository

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.source.local.datasource.contact.ContactLocalDataSource
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.utility.sharedViewModels.contactProfile.IContractContactProfileShare
import javax.inject.Inject

class ContactProfileShareRepository
@Inject constructor(
    private val contactLocalDataSource: ContactLocalDataSource
) : IContractContactProfileShare.Repository {

    override fun getLocalContact(contactId: Int): LiveData<ContactEntity> {
        return contactLocalDataSource.getContact(contactId)
    }
}