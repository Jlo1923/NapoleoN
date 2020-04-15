package com.naposystems.pepito.repository.sharedRepository

import androidx.lifecycle.LiveData
import com.naposystems.pepito.db.dao.contact.ContactDataSource
import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.utility.sharedViewModels.contactProfile.IContractContactProfileShare
import javax.inject.Inject

class ContactProfileShareRepository @Inject constructor(
    private val contactLocalDataSource: ContactDataSource
) : IContractContactProfileShare.Repository {

    override fun getLocalContact(contactId: Int): LiveData<Contact> {
        return contactLocalDataSource.getContact(contactId)
    }
}