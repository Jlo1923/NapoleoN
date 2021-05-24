package com.naposystems.napoleonchat.repository.contacts

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.source.local.entity.ContactEntity

interface ContactsRepository {
    suspend fun getLocalContacts(): LiveData<MutableList<ContactEntity>>
    suspend fun getLocalContactsForSearch(): LiveData<MutableList<ContactEntity>>
}