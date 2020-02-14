package com.naposystems.pepito.db.dao.contact

import androidx.lifecycle.LiveData
import com.naposystems.pepito.entity.Contact
import javax.inject.Inject

class ContactLocalDataSource @Inject constructor(private val contactDao: ContactDao) :
    ContactDataSource {

    override suspend fun getContacts(): LiveData<List<Contact>> {
        return contactDao.getContacts()
    }

    override suspend fun getLocaleContacts(): List<Contact> {
        return contactDao.getLocalContacts()
    }

    override suspend fun insertContact(contact: Contact) {
        contactDao.insertContact(contact)
    }

    override suspend fun insertContactList(contactList: List<Contact>, delete: Boolean) {

        if (delete) {
            val localContacts = getLocaleContacts()

            val contactsDeleted = localContacts.subtract(contactList)

            if (contactsDeleted.isNotEmpty()) {
                contactDao.deleteContacts(contactsDeleted.toList())
            }
        }

        for (contact in contactList) {

            val contactById = contactDao.getContactById(contact.id)

            if (contactById.isNotEmpty()) {
                contactDao.updateContact(contact)
            } else {
                contactDao.insertContact(contact)
            }
        }
    }
}