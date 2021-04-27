package com.naposystems.napoleonchat.source.local.datasource.contact

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.source.local.dao.ContactDao
import com.naposystems.napoleonchat.utility.Constants
import javax.inject.Inject

class ContactLocalDataSourceImp @Inject constructor(private val contactDao: ContactDao) :
    ContactLocalDataSource {


    override suspend fun updateAvatarFakeContact(contactId: Int, avatarFake: String) {
        contactDao.updateAvatarFakeContact(contactId, avatarFake)
    }

    override suspend fun restoreContact(contactId: Int) {
        contactDao.restoreContact(contactId)
    }

    override fun getContact(contactId: Int): LiveData<ContactEntity> {
        return contactDao.getContact(contactId)
    }

    override fun getContactById(contactId: Int): ContactEntity? {
        return contactDao.getContactById(contactId)
    }

    override suspend fun getContacts(): LiveData<MutableList<ContactEntity>> {
        return contactDao.getContacts()
    }

    override suspend fun getLocaleContacts(): List<ContactEntity> {
        return contactDao.getLocalContacts()
    }

    override suspend fun insertContact(contact: ContactEntity) {
        contactDao.insertContact(contact)
    }

    override suspend fun insertOrUpdateContactList(
        contactList: List<ContactEntity>,
        location: Int
    ): List<ContactEntity> {
        contactList.forEach { remoteContact ->
            val localContact = contactDao.getContactById(remoteContact.id)
            if (localContact != null) {
                if (location == Constants.LocationGetContact.OTHER.location) {
                    localContact.apply {
                        imageUrlFake = remoteContact.imageUrlFake
                        displayNameFake = remoteContact.displayNameFake
                        nicknameFake = remoteContact.nicknameFake
                        imageUrl = remoteContact.imageUrl
                        displayName = remoteContact.displayName
                        status = remoteContact.status
                        lastSeen = remoteContact.lastSeen
                        contactDao.updateContact(localContact)
                    }

                }
            } else {
                contactDao.insertContact(remoteContact)
            }
        }

        val localContacts = getLocaleContacts()
        return localContacts.subtract(contactList).toList()
    }

    override fun getBlockedContacts(): LiveData<List<ContactEntity>> {
        return contactDao.getBlockedContacts()
    }

    override suspend fun blockContact(contactId: Int) {
        contactDao.blockContact(contactId)
    }

    override suspend fun unblockContact(contactId: Int) {
        contactDao.unblockContact(contactId)
    }

    override suspend fun deleteContact(contact: ContactEntity) {
        contactDao.deleteContact(contact)
    }

    override suspend fun deleteContacts(contacts: List<ContactEntity>) {
        contactDao.deleteContacts(contacts)
    }

    override suspend fun updateContactSilenced(contactId: Int, contactSilenced: Int) {
        contactDao.updateContactSilenced(contactId, contactSilenced)
    }

    override suspend fun getContactSilenced(contactId: Int): Boolean {
        return contactDao.getContactSilenced(contactId)
    }

    override suspend fun setSelfDestructTimeByContact(selfDestructTime: Int, contactId: Int) {
        contactDao.setSelfDestructTimeByContact(selfDestructTime, contactId)
    }

    override suspend fun getSelfDestructTimeByContact(contactId: Int): LiveData<Int> {
        return contactDao.getSelfDestructTimeByContact(contactId)
    }

    override suspend fun getSelfDestructTimeAsIntByContact(contactId: Int): Int {
        return contactDao.getSelfDestructTimeAsIntByContact(contactId)
    }

    override suspend fun restoreImageByContact(contactId: Int) {
        contactDao.restoreImageByContact(contactId)
    }

    override suspend fun updateChannelId(contactId: Int, channelId: String) {
        contactDao.updateChannelId(contactId, channelId)
    }

    override suspend fun updateStateChannel(contactId: Int, state: Boolean) {
        contactDao.updateStateChannel(contactId, state)
    }

    override suspend fun updateContact(contact: ContactEntity) {
        contactDao.updateContact(contact)
    }
}