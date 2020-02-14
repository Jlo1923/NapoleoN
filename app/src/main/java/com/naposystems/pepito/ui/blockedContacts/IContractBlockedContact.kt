package com.naposystems.pepito.ui.blockedContacts

import androidx.lifecycle.LiveData
import com.naposystems.pepito.dto.contacts.unblockContact.UnblockContactErrorDTO
import com.naposystems.pepito.dto.contacts.unblockContact.UnblockContactResDTO
import com.naposystems.pepito.entity.Contact
import okhttp3.ResponseBody
import retrofit2.Response

interface IContractBlockedContact {

    interface ViewModel {
        fun getBlockedContacts()
        fun searchLocalBlockedContact(query: String)
        fun unblockContact(contact: Contact)
    }

    interface Repository {
        suspend fun getRemoteBlockedContacts()
        suspend fun getBlockedContactsLocal(): LiveData<List<Contact>>
        suspend fun unblockContact(contact: Contact): Response<UnblockContactResDTO>
        suspend fun unblockContactLocal(contactId: Int)
        fun getDefaultError(response: ResponseBody): ArrayList<String>
    }
}