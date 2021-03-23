package com.naposystems.napoleonchat.repository.contactProfile

import com.naposystems.napoleonchat.source.local.datasource.contact.ContactLocalDataSource
import com.naposystems.napoleonchat.source.remote.dto.muteConversation.MuteConversationErrorDTO
import com.naposystems.napoleonchat.source.remote.dto.muteConversation.MuteConversationReqDTO
import com.naposystems.napoleonchat.source.remote.dto.muteConversation.MuteConversationResDTO
import com.naposystems.napoleonchat.ui.contactProfile.IContractContactProfile
import com.naposystems.napoleonchat.source.remote.api.NapoleonApi
import com.naposystems.napoleonchat.source.remote.dto.contactProfile.ContactFakeReqDTO
import com.naposystems.napoleonchat.source.remote.dto.contactProfile.ContactFakeResDTO
import com.squareup.moshi.Moshi
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

class ContactProfileRepository @Inject constructor(
    private val napoleonApi: NapoleonApi,
    private val contactLocalDataSource: ContactLocalDataSource
) : IContractContactProfile.Repository {

    private val moshi: Moshi by lazy {
        Moshi.Builder().build()
    }

    override suspend fun updateAvatarFakeContact(
        contactId: Int,
        avatarFake: String
    ): Response<ContactFakeResDTO> {
        val request = ContactFakeReqDTO(null, null, avatarFake)
        return napoleonApi.updateContactFake(request, contactId)
    }

    override suspend fun restoreContact(contactId: Int): Response<ContactFakeResDTO> {
        val request = ContactFakeReqDTO("", "", "")
        return napoleonApi.updateContactFake(request, contactId)
    }

    override suspend fun updateContactSilenced(contactId: Int, contactSilenced: Int) {
        contactLocalDataSource.updateContactSilenced(contactId, contactSilenced)
    }

    override suspend fun saveTimeMuteConversation(
        contactId: Int,
        time: MuteConversationReqDTO
    ): Response<MuteConversationResDTO> {
        return napoleonApi.updateMuteConversation(contactId, time)
    }

    override fun getError(response: Response<MuteConversationResDTO>): ArrayList<String> {
        val adapter = moshi.adapter(MuteConversationErrorDTO::class.java)
        val error = adapter.fromJson(response.errorBody()!!.string())
        val errorList = ArrayList<String>()
        errorList.add(error!!.error)
        return errorList
    }

    override suspend fun restoreImageByContact(contactId: Int) {
        contactLocalDataSource.restoreImageByContact(contactId)
    }

    override suspend fun updateContactFakeLocal(
        contactId: Int,
        contactUpdated: ContactFakeResDTO,
        isRestored: Boolean
    ) {

        val contact = contactLocalDataSource.getContactById(contactId)
        contact?.let {
            contact.displayName = contactUpdated.fullname
            contact.nickname = contactUpdated.nickname
            contact.imageUrl = contactUpdated.avatar ?: ""
            contact.displayNameFake =
                if (contactUpdated.fullNameFake.isNullOrEmpty()) contactUpdated.fullname else contactUpdated.fullNameFake
            contact.nicknameFake =
                if (contactUpdated.nicknameFake.isNullOrEmpty()) contactUpdated.nickname else contactUpdated.nicknameFake
            contact.imageUrlFake =
                if (contactUpdated.avatarFake.isNullOrEmpty()) contactUpdated.avatar
                    ?: "" else contactUpdated.avatarFake
            if (isRestored) contact.stateNotification = false

            contactLocalDataSource.updateContact(contact)
        }

    }
}