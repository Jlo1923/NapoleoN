package com.naposystems.pepito.db.dao.contact

import androidx.lifecycle.LiveData
import com.naposystems.pepito.entity.Contact
import javax.inject.Inject

class ContactLocalDataSource @Inject constructor(private val contactDao: ContactDao) :
    ContactDataSource {

    override fun getContact(idContact : Int): LiveData<Contact> {
        return contactDao.getContact(idContact)
    }

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
        contactDao.insertContacts(contactList)

        if (delete) {
            val localContacts = getLocaleContacts()

            val contactsDeleted = localContacts.subtract(contactList)

            if (contactsDeleted.isNotEmpty()) {
                deleteContacts(contactsDeleted.toList())
            }
        }
    }

    override suspend fun deleteContacts(contacts: List<Contact>) {
        contactDao.deleteContacts(contacts)
    }
}