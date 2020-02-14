package com.naposystems.pepito.ui.contacts

import androidx.lifecycle.LiveData
import com.naposystems.pepito.dto.contacts.blockedContact.BlockedContactResDTO
import com.naposystems.pepito.dto.contacts.deleteContact.DeleteContactResDTO
import com.naposystems.pepito.entity.Contact
import retrofit2.Response

interface IContractContacts {

    interface ViewModel {
        fun getContacts()
        fun sendBlockedContact(contact: Contact)
        fun sendDeleteContact(contact: Contact)
        fun resetContactsLoaded()
    }

    interface Repository {
        suspend fun getLocalContacts(): LiveData<List<Contact>>
        suspend fun getRemoteContacts()
        suspend fun sendBlockedContact(contact: Contact) : Response<BlockedContactResDTO>
        suspend fun blockContactLocal(contactId: Int)
        suspend fun sendDeleteContact(contact: Contact) : Response<DeleteContactResDTO>
        suspend fun deleteContactLocal(contact: Contact)
        fun getDefaultBlockedError(response: Response<BlockedContactResDTO>): List<String>
        fun getDefaultDeleteError(response: Response<DeleteContactResDTO>): List<String>
    }
}