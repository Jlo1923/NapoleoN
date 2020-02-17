package com.naposystems.pepito.repository.contactProfile

import androidx.lifecycle.LiveData
import com.naposystems.pepito.db.dao.contact.ContactDataSource
import com.naposystems.pepito.dto.muteConversation.MuteConversationErrorDTO
import com.naposystems.pepito.dto.muteConversation.MuteConversationReqDTO
import com.naposystems.pepito.dto.muteConversation.MuteConversationResDTO
import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.ui.contactProfile.IContactProfile
import com.naposystems.pepito.webService.NapoleonApi
import com.squareup.moshi.Moshi
import retrofit2.Response
import javax.inject.Inject

class ContactProfileRepository@Inject constructor(
    private val napoleonApi: NapoleonApi,
    private val contactDataSource: ContactDataSource
) : IContactProfile.Repository {

    private val moshi: Moshi by lazy {
        Moshi.Builder().build()
    }

    override fun getLocalContact(idContact: Int): LiveData<Contact> {
        return contactDataSource.getContact(idContact)
    }

    override suspend fun updateNameFakeLocalContact(idContact: Int, nameFake: String) {
        contactDataSource.updateNameFakeLocalContact(idContact, nameFake)
    }

    override suspend fun updateNicknameFakeLocalContact(idContact: Int, nicknameFake: String) {
        contactDataSource.updateNicknameFakeLocalContact(idContact, nicknameFake)
    }

    override suspend fun updateAvatarFakeLocalContact(idContact: Int, avatarFake: String) {
        contactDataSource.updateAvatarFakeLocalContact(idContact, avatarFake)
    }

    override suspend fun restoreLocalContact(idContact: Int) {
        contactDataSource.restoreLocalContact(idContact)
    }

    override suspend fun deleteConversation(idContact: Int) {
        contactDataSource.deleteMessages(idContact)
        contactDataSource.cleanConversation(idContact)
    }

    override suspend fun updateContactSilenced(idContact: Int, contactSilenced: Int) {
        contactDataSource.updateContactSilenced(idContact, contactSilenced)
    }

    override suspend fun saveTimeMuteConversation(idContact: Int, time: MuteConversationReqDTO): Response<MuteConversationResDTO> {
        return napoleonApi.updateMuteConversation(idContact, time)
    }

    override fun getError(response: Response<MuteConversationResDTO>): ArrayList<String> {
        val adapter = moshi.adapter(MuteConversationErrorDTO::class.java)
        val error = adapter.fromJson(response.errorBody()!!.string())
        val errorList = ArrayList<String>()
        errorList.add(error!!.error)
        return errorList
    }
}