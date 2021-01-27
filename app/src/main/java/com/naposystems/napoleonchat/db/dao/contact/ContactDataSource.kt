package com.naposystems.napoleonchat.db.dao.contact

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.entity.Contact

interface ContactDataSource {

    suspend fun getContacts(): LiveData<MutableList<Contact>>

    suspend fun getLocaleContacts(): List<Contact>

    fun getContact(contactId : Int): LiveData<Contact>

    fun getContactById(contactId : Int): Contact?

    suspend fun restoreContact(contactId: Int)

    suspend fun updateNameFakeContact(contactId: Int, nameFake: String)

    suspend fun updateNicknameFakeContact(contactId: Int, nicknameFake: String)

    suspend fun updateAvatarFakeContact(contactId: Int, avatarFake: String)

    suspend fun insertContact(contact: Contact)

    suspend fun insertOrUpdateContactList(contactList: List<Contact>, location : Int = 0): List<Contact>

    fun getBlockedContacts() : LiveData<List<Contact>>

    suspend fun blockContact(contactId: Int)

    suspend fun unblockContact(contactId: Int)

    suspend fun deleteContact(contact: Contact)

    suspend fun deleteContacts(contacts: List<Contact>)

    suspend fun updateContactSilenced(contactId: Int, contactSilenced : Int)

    suspend fun getContactSilenced(contactId: Int) : Boolean

    suspend fun setSelfDestructTimeByContact(selfDestructTime: Int, contactId: Int)

    suspend fun getSelfDestructTimeByContact(contactId: Int) : LiveData<Int>

    suspend fun restoreImageByContact(contactId: Int)

    suspend fun updateChannelId(contactId: Int, channelId: String)

    suspend fun updateStateChannel(contactId: Int, state:Boolean)
}