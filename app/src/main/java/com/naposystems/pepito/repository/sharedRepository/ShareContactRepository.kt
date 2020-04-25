package com.naposystems.pepito.repository.sharedRepository

import com.naposystems.pepito.db.dao.contact.ContactDataSource
import com.naposystems.pepito.db.dao.message.MessageLocalDataSource
import com.naposystems.pepito.dto.contacts.blockedContact.BlockedContactResDTO
import com.naposystems.pepito.dto.contacts.deleteContact.DeleteContactErrorDTO
import com.naposystems.pepito.dto.contacts.deleteContact.DeleteContactResDTO
import com.naposystems.pepito.dto.contacts.unblockContact.UnblockContactErrorDTO
import com.naposystems.pepito.dto.contacts.unblockContact.UnblockContactResDTO
import com.naposystems.pepito.dto.muteConversation.MuteConversationErrorDTO
import com.naposystems.pepito.dto.muteConversation.MuteConversationReqDTO
import com.naposystems.pepito.dto.muteConversation.MuteConversationResDTO
import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.utility.sharedViewModels.contact.IContractShareContact
import com.naposystems.pepito.webService.NapoleonApi
import com.squareup.moshi.Moshi
import retrofit2.Response
import javax.inject.Inject

class ShareContactRepository @Inject constructor(
    private val napoleonApi: NapoleonApi,
    private val contactLocalDataSource: ContactDataSource,
    private val messageLocalDataSource: MessageLocalDataSource
) : IContractShareContact.Repository {

    private val moshi by lazy {
        Moshi.Builder().build()
    }

    override suspend fun sendBlockedContact(contact: Contact): Response<BlockedContactResDTO> {
        return napoleonApi.putBlockContact(contact.id.toString())
    }

    override suspend fun blockContactLocal(contact: Contact) {
        contactLocalDataSource.blockContact(contact.id)
        deleteConversation(contact.id)
    }

    override suspend fun unblockContact(contactId: Int): Response<UnblockContactResDTO> {
        return napoleonApi.putUnblockContact(contactId.toString())
    }

    override suspend fun unblockContactLocal(contactId: Int) {
        contactLocalDataSource.unblockContact(contactId)
    }

    override suspend fun sendDeleteContact(contact: Contact): Response<DeleteContactResDTO> {
        return napoleonApi.sendDeleteContact(contact.id.toString())
    }

    override suspend fun deleteContactLocal(contact: Contact) {
        contactLocalDataSource.deleteContact(contact)
    }

    override suspend fun deleteConversation(contactId: Int) {
        messageLocalDataSource.deleteMessages(contactId)
    }

    override suspend fun muteConversation(contactId: Int,time: MuteConversationReqDTO
    ): Response<MuteConversationResDTO> {
        return napoleonApi.updateMuteConversation(contactId, time)
    }

    override suspend fun muteConversationLocal(contactId: Int, contactSilenced: Int) {
        contactLocalDataSource.updateContactSilenced(contactId, contactSilenced)
    }

    override fun getDefaultDeleteError(response: Response<DeleteContactResDTO>): List<String> {
        val adapter = moshi.adapter(DeleteContactErrorDTO::class.java)
        val updateUserInfoError = adapter.fromJson(response.errorBody()!!.string())

        return arrayListOf(updateUserInfoError!!.error)
    }

    override fun getDefaultUnblockError(response: Response<UnblockContactResDTO>): List<String> {
        val adapter = moshi.adapter(UnblockContactErrorDTO::class.java)
        val updateUserInfoError = adapter.fromJson(response.errorBody()!!.string())

        return arrayListOf(updateUserInfoError!!.error)
    }

    override fun getDefaultBlockedError(response: Response<BlockedContactResDTO>): List<String> {
        val adapter = moshi.adapter(DeleteContactErrorDTO::class.java)
        val updateUserInfoError = adapter.fromJson(response.errorBody()!!.string())

        return arrayListOf(updateUserInfoError!!.error)
    }

    override fun muteError(response: Response<MuteConversationResDTO>): ArrayList<String> {
        val adapter = moshi.adapter(MuteConversationErrorDTO::class.java)
        val error = adapter.fromJson(response.errorBody()!!.string())
        val errorList = ArrayList<String>()
        errorList.add(error!!.error)
        return errorList
    }
}