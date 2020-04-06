package com.naposystems.pepito.repository.contactProfile

import androidx.lifecycle.LiveData
import com.naposystems.pepito.db.dao.contact.ContactDataSource
import com.naposystems.pepito.dto.muteConversation.MuteConversationErrorDTO
import com.naposystems.pepito.dto.muteConversation.MuteConversationReqDTO
import com.naposystems.pepito.dto.muteConversation.MuteConversationResDTO
import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.ui.contactProfile.IContractContactProfile
import com.naposystems.pepito.webService.NapoleonApi
import com.squareup.moshi.Moshi
import retrofit2.Response
import javax.inject.Inject

class ContactProfileRepository@Inject constructor(
    private val napoleonApi: NapoleonApi,
    private val contactDataSource: ContactDataSource
) : IContractContactProfile.Repository {

    private val moshi: Moshi by lazy {
        Moshi.Builder().build()
    }

    override fun getLocalContact(contactId: Int): LiveData<Contact> {
        return contactDataSource.getContact(contactId)
    }

    override suspend fun updateNameFakeContact(contactId: Int, nameFake: String) {
        contactDataSource.updateNameFakeContact(contactId, nameFake)
    }

    override suspend fun updateNicknameFakeContact(contactId: Int, nicknameFake: String) {
        contactDataSource.updateNicknameFakeContact(contactId, nicknameFake)
    }

    override suspend fun updateAvatarFakeContact(contactId: Int, avatarFake: String) {
        contactDataSource.updateAvatarFakeContact(contactId, avatarFake)
    }

    override suspend fun restoreContact(contactId: Int) {
        contactDataSource.restoreContact(contactId)
    }

    override suspend fun updateContactSilenced(contactId: Int, contactSilenced: Int) {
        contactDataSource.updateContactSilenced(contactId, contactSilenced)
    }

    override suspend fun saveTimeMuteConversation(contactId: Int, time: MuteConversationReqDTO): Response<MuteConversationResDTO> {
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
        contactDataSource.restoreImageByContact(contactId)
    }
}