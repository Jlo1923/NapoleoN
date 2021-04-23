package com.naposystems.napoleonchat.utility.sharedViewModels.contact

import com.naposystems.napoleonchat.source.remote.dto.contacts.blockedContact.BlockedContactResDTO
import com.naposystems.napoleonchat.source.remote.dto.contacts.deleteContact.DeleteContactResDTO
import com.naposystems.napoleonchat.source.remote.dto.contacts.unblockContact.UnblockContactResDTO
import com.naposystems.napoleonchat.source.remote.dto.muteConversation.MuteConversationReqDTO
import com.naposystems.napoleonchat.source.remote.dto.muteConversation.MuteConversationResDTO
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import retrofit2.Response

interface ContactSharedRepository {

        suspend fun getContacts(state: String, location: Int): Boolean
        //region Block Contact
        suspend fun sendBlockedContact(contact: ContactEntity) : Response<BlockedContactResDTO>
        suspend fun blockContactLocal(contact: ContactEntity)
        //endregion
        //region Unblock Contact
        suspend fun unblockContact(contactId: Int): Response<UnblockContactResDTO>
        suspend fun unblockContactLocal(contactId: Int)
        //endregion
        //region Delete Contact
        suspend fun sendDeleteContact(contact: ContactEntity) : Response<DeleteContactResDTO>
        suspend fun deleteContactLocal(contact: ContactEntity)
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