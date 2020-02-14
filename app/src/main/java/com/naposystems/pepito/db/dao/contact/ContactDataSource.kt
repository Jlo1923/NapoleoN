package com.naposystems.pepito.db.dao.contact

import androidx.lifecycle.LiveData
import com.naposystems.pepito.entity.Contact

interface ContactDataSource {

    suspend fun getContacts(): LiveData<List<Contact>>

    suspend fun getLocaleContacts(): List<Contact>

    fun getContact(idContact : Int): LiveData<Contact>

    suspend fun restoreLocalContact(idContact: Int)

    suspend fun deleteMessages(idContact: Int)

    suspend fun cleanConversation(idContact: Int)

    suspend fun updateNameFakeLocalContact(idContact: Int, nameFake: String)

    suspend fun updateNicknameFakeLocalContact(idContact: Int, nicknameFake: String)

    suspend fun updateAvatarFakeLocalContact(idContact: Int, avatarFake: String)

    suspend fun insertContact(contact: Contact)

    suspend fun insertContactList(contactList: List<Contact>)

    fun getBlockedContacts() : LiveData<List<Contact>>

    suspend fun blockContact(contactId: Int)

    suspend fun unblockContact(contactId: Int)

    suspend fun deleteContact(contact: Contact)

    suspend fun deleteContacts(contacts: List<Contact>)

    suspend fun updateContactSilenced(idContact: Int, contactSilenced : Int)

}