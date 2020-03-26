package com.naposystems.pepito.db.dao.contact

import androidx.lifecycle.LiveData
import com.naposystems.pepito.entity.Contact
import javax.inject.Inject

class ContactLocalDataSource @Inject constructor(private val contactDao: ContactDao) :
    ContactDataSource {

    override suspend fun updateNicknameFakeContact(contactId: Int, nicknameFake: String) {
        contactDao.updateNickNameFakeContact(contactId, nicknameFake)
    }

    override suspend fun updateNameFakeContact(contactId: Int, nameFake: String) {
        contactDao.updateNameFakeContact(contactId, nameFake)
    }

    override suspend fun updateAvatarFakeContact(contactId: Int, avatarFake: String) {
        contactDao.updateAvatarFakeContact(contactId, avatarFake)
    }

    override suspend fun restoreContact(contactId: Int) {
        contactDao.restoreContact(contactId)
    }

    override fun getContact(contactId : Int): LiveData<Contact> {
        return contactDao.getContact(contactId)
    }

    override fun getContactById(contactId: Int): Contact {
        return contactDao.getContactById(contactId)
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

    override suspend fun insertContactList(contactList: List<Contact>) {
        contactDao.insertContacts(contactList)

        val localContacts = getLocaleContacts()

        val contactsDeleted = localContacts.subtract(contactList)

        if (contactsDeleted.isNotEmpty()) {
            deleteContacts(contactsDeleted.toList())
        }
    }

    override fun getBlockedContacts(): LiveData<List<Contact>> {
        return contactDao.getBlockedContacts()
    }

    override suspend fun blockContact(contactId: Int) {
        contactDao.blockContact(contactId)
    }

    override suspend fun unblockContact(contactId: Int) {
        contactDao.unblockContact(contactId)
    }

    override suspend fun deleteContact(contact: Contact) {
        contactDao.deleteContact(contact)
    }

    override suspend fun deleteContacts(contacts: List<Contact>) {
        contactDao.deleteContacts(contacts)
    }

    override suspend fun updateContactSilenced(contactId: Int, contactSilenced: Int) {
        contactDao.updateContactSilenced(contactId, contactSilenced)
    }

    override suspend fun setSelfDestructTimeByContact(selfDestructTime: Int, contactId: Int) {
        contactDao.setSelfDestructTimeByContact(selfDestructTime, contactId)
    }

    override suspend fun getSelfDestructTimeByContact(contactId: Int) : LiveData<Int> {
        return contactDao.getSelfDestructTimeByContact(contactId)
    }
}