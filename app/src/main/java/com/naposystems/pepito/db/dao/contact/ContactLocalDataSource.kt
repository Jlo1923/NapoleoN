package com.naposystems.pepito.db.dao.contact

import androidx.lifecycle.LiveData
import com.naposystems.pepito.entity.Contact
import javax.inject.Inject

class ContactLocalDataSource @Inject constructor(private val contactDao: ContactDao) :
    ContactDataSource {

    override suspend fun updateNicknameFakeContact(idContact: Int, nicknameFake: String) {
        contactDao.updateNickNameFakeContact(idContact, nicknameFake)
    }

    override suspend fun updateNameFakeContact(idContact: Int, nameFake: String) {
        contactDao.updateNameFakeContact(idContact, nameFake)
    }

    override suspend fun updateAvatarFakeContact(idContact: Int, avatarFake: String) {
        contactDao.updateAvatarFakeContact(idContact, avatarFake)
    }

    override suspend fun restoreContact(idContact: Int) {
        contactDao.restoreContact(idContact)
    }

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

    override suspend fun updateContactSilenced(idContact: Int, contactSilenced: Int) {
        contactDao.updateContactSilenced(idContact, contactSilenced)
    }

    override suspend fun setSelfDestructTimeByContact(selfDestructTime: Int, contactId: Int) {
        contactDao.setSelfDestructTimeByContact(selfDestructTime, contactId)
    }

    override suspend fun getSelfDestructTimeByContact(contactId: Int) : LiveData<Int> {
        return contactDao.getSelfDestructTimeByContact(contactId)
    }
}