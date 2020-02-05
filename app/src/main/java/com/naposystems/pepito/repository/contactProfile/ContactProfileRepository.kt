package com.naposystems.pepito.repository.contactProfile

import androidx.lifecycle.LiveData
import com.naposystems.pepito.db.dao.contact.ContactDataSource
import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.ui.contactProfile.IContactProfile
import javax.inject.Inject

class ContactProfileRepository@Inject constructor(
    private val contactDataSource: ContactDataSource
) : IContactProfile.Repository {

    override fun getLocalContact(idContact: Int): LiveData<Contact> {
        return contactDataSource.getContact(idContact)
    }

}