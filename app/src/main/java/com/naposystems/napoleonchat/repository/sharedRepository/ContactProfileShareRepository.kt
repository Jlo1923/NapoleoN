package com.naposystems.napoleonchat.repository.sharedRepository

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.db.dao.contact.ContactDataSource
import com.naposystems.napoleonchat.entity.Contact
import com.naposystems.napoleonchat.utility.sharedViewModels.contactProfile.IContractContactProfileShare
import javax.inject.Inject

class ContactProfileShareRepository
@Inject constructor(
    private val contactLocalDataSource: ContactDataSource
) : IContractContactProfileShare.Repository {

    override fun getLocalContact(contactId: Int): LiveData<Contact> {
        return contactLocalDataSource.getContact(contactId)
    }
}