package com.naposystems.napoleonchat.utility.sharedViewModels.contact

import com.naposystems.napoleonchat.dto.contacts.blockedContact.BlockedContactResDTO
import com.naposystems.napoleonchat.dto.contacts.deleteContact.DeleteContactResDTO
import com.naposystems.napoleonchat.dto.contacts.unblockContact.UnblockContactResDTO
import com.naposystems.napoleonchat.dto.muteConversation.MuteConversationReqDTO
import com.naposystems.napoleonchat.dto.muteConversation.MuteConversationResDTO
import com.naposystems.napoleonchat.entity.Contact
import retrofit2.Response

interface IContractShareContact {
    interface ViewModel {
        fun sendBlockedContact(contact: Contact)
        fun unblockContact(contactId: Int)
        fun sendDeleteContact(contact: Contact)
        fun deleteConversation(contactId: Int)
        fun muteConversation(contactId: Int, contactSilenced: Boolean)
    }

    interface Repository {
        //region Block Contact
        suspend fun sendBlockedContact(contact: Contact) : Response<BlockedContactResDTO>
        suspend fun blockContactLocal(contact: Contact)
        //endregion
        //region Unblock Contact
        suspend fun unblockContact(contactId: Int): Response<UnblockContactResDTO>
        suspend fun unblockContactLocal(contactId: Int)
        //endregion
        //region Delete Contact
        suspend fun sendDeleteContact(contact: Contact) : Response<DeleteContactResDTO>
        suspend fun deleteContactLocal(contact: Contact)
        //endregion
        //region Delete Conversation
        suspend fun deleteConversation(contactId: Int)
        //endregion
        //region Mute Conversation
        suspend fun muteConversation(contactId : Int, time: MuteConversationReqDTO) : Response<MuteConversationResDTO>
        suspend fun muteConversationLocal(contactId : Int, contactSilenced: Int)
        //endregion
        //region Errors
        fun getDefaultBlockedError(response: Response<BlockedContactResDTO>): List<String>
        fun getDefaultUnblockError(response: Response<UnblockContactResDTO>): List<String>
        fun getDefaultDeleteError(response: Response<DeleteContactResDTO>): List<String>
        fun muteError(response: Response<MuteConversationResDTO>): ArrayList<String>
        //endregion
    }
}