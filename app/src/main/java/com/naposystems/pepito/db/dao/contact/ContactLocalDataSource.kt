package com.naposystems.pepito.db.dao.contact

import androidx.lifecycle.LiveData
import com.naposystems.pepito.entity.Contact
import javax.inject.Inject

class ContactLocalDataSource @Inject constructor(private val contactDao: ContactDao) :
    ContactDataSource {

    override suspend fun updateNicknameFakeLocalContact(idContact: Int, nicknameFake: String) {
        contactDao.updateNickNameFakeContact(idContact, nicknameFake)
    }

    override suspend fun updateNameFakeLocalContact(idContact: Int, nameFake: String) {
        contactDao.updateNameFakeLocalContact(idContact, nameFake)
    }

    override suspend fun updateAvatarFakeLocalContact(idContact: Int, avatarFake: String) {
        contactDao.updateAvatarFakeLocalContact(idContact, avatarFake)
    }

    override suspend fun restoreLocalContact(idContact: Int) {
        contactDao.restoreLocalContact(idContact)
    }

    override suspend fun deleteMessages(idContact: Int) {
        contactDao.deleteMessages(idContact)
    }

    override suspend fun cleanConversation(idContact: Int) {
        contactDao.cleanConversation(idContact)
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
}