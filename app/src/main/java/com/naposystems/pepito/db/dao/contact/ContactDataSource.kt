package com.naposystems.pepito.db.dao.contact

import androidx.lifecycle.LiveData
import com.naposystems.pepito.entity.Contact

interface ContactDataSource {

    suspend fun getContacts(): LiveData<List<Contact>>

    suspend fun getLocaleContacts(): List<Contact>

    suspend fun insertContact(contact: Contact)

    suspend fun insertContactList(contactList: List<Contact>, delete: Boolean)

    suspend fun deleteContacts(contacts: List<Contact>)
}