package com.naposystems.pepito.db.dao.contact

import androidx.lifecycle.LiveData
import com.naposystems.pepito.entity.Contact

interface ContactDataSource {

    suspend fun getContacts(): LiveData<List<Contact>>

    suspend fun getLocaleContacts(): List<Contact>

    fun getContact(idContact : Int): LiveData<Contact>

    suspend fun restoreContact(idContact: Int)

    suspend fun updateNameFakeContact(idContact: Int, nameFake: String)

    suspend fun updateNicknameFakeContact(idContact: Int, nicknameFake: String)

    suspend fun updateAvatarFakeContact(idContact: Int, avatarFake: String)

    suspend fun insertContact(contact: Contact)

    suspend fun insertContactList(contactList: List<Contact>, delete: Boolean)

    suspend fun deleteContacts(contacts: List<Contact>)

    suspend fun updateContactSilenced(idContact: Int, contactSilenced : Int)

}