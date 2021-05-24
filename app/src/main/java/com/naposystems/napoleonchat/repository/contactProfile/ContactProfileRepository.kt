package com.naposystems.napoleonchat.repository.contactProfile

import com.naposystems.napoleonchat.source.remote.dto.contactProfile.ContactFakeResDTO
import com.naposystems.napoleonchat.source.remote.dto.muteConversation.MuteConversationReqDTO
import com.naposystems.napoleonchat.source.remote.dto.muteConversation.MuteConversationResDTO
import retrofit2.Response

interface ContactProfileRepository {
    suspend fun updateAvatarFakeContact(
        contactId: Int,
        avatarFake: String
    ): Response<ContactFakeResDTO>

    suspend fun restoreContact(contactId: Int): Response<ContactFakeResDTO>
    suspend fun updateContactSilenced(contactId: Int, contactSilenced: Int)
    suspend fun saveTimeMuteConversation(
        contactId: Int,
        time: MuteConversationReqDTO
    ): Response<MuteConversationResDTO>

    fun getError(response: Response<MuteConversationResDTO>): ArrayList<String>
    suspend fun restoreImageByContact(contactId: Int)
    suspend fun updateContactFakeLocal(
        contactId: Int,
        contactUpdated: ContactFakeResDTO,
        isRestored: Boolean
    )
}