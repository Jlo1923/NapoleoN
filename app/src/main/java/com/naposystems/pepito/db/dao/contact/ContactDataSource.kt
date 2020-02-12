package com.naposystems.pepito.db.dao.contact

import androidx.lifecycle.LiveData
import com.naposystems.pepito.entity.Contact

interface ContactDataSource {

    suspend fun getContacts(): LiveData<List<Contact>>

    suspend fun getLocaleContacts(): List<Contact>

    suspend fun insertContact(contact: Contact)

    suspend fun insertContactList(contactList: List<Contact>, delete: Boolean)

    fun getBlockedContacts() : LiveData<List<Contact>>

    suspend fun blockContact(contactId: Int)

    suspend fun unblockContact(contactId: Int)

    suspend fun deleteContact(contact: Contact)

    suspend fun deleteContacts(contacts: List<Contact>)
}