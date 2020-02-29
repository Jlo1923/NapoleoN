package com.naposystems.pepito.utility.sharedViewModels.contact

import com.naposystems.pepito.dto.contacts.blockedContact.BlockedContactResDTO
import com.naposystems.pepito.dto.contacts.deleteContact.DeleteContactResDTO
import com.naposystems.pepito.dto.contacts.unblockContact.UnblockContactErrorDTO
import com.naposystems.pepito.dto.contacts.unblockContact.UnblockContactResDTO
import com.naposystems.pepito.entity.Contact
import retrofit2.Response

interface IContractShareContact {
    interface ViewModel {
        fun sendBlockedContact(contact: Contact)
        fun unblockContact(contact: Contact)
        fun sendDeleteContact(contact: Contact)
        fun deleteConversation(contactId: Int)
    }

    interface Repository {
        //region Block Contact
        suspend fun sendBlockedContact(contact: Contact) : Response<BlockedContactResDTO>
        suspend fun blockContactLocal(contact: Contact)
        //endregion
        //region Unblock Contact
        suspend fun unblockContact(contact: Contact): Response<UnblockContactResDTO>
        suspend fun unblockContactLocal(contactId: Int)
        //endregion
        //region Delete Contact
        suspend fun sendDeleteContact(contact: Contact) : Response<DeleteContactResDTO>
        suspend fun deleteContactLocal(contact: Contact)
        //endregion
        //region Delete Conversation
        suspend fun deleteConversation(contactId: Int)
        //endregion
        //region Errors
        fun getDefaultBlockedError(response: Response<BlockedContactResDTO>): List<String>
        fun getDefaultUnblockError(response: Response<UnblockContactResDTO>): List<String>
        fun getDefaultDeleteError(response: Response<DeleteContactResDTO>): List<String>
        //endregion
    }
}