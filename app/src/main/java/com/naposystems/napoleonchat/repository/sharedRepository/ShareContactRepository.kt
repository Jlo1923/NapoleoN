package com.naposystems.napoleonchat.repository.sharedRepository

import com.naposystems.napoleonchat.db.dao.contact.ContactDataSource
import com.naposystems.napoleonchat.db.dao.message.MessageLocalDataSource
import com.naposystems.napoleonchat.dto.contacts.blockedContact.BlockedContactResDTO
import com.naposystems.napoleonchat.dto.contacts.deleteContact.DeleteContactErrorDTO
import com.naposystems.napoleonchat.dto.contacts.deleteContact.DeleteContactResDTO
import com.naposystems.napoleonchat.dto.contacts.unblockContact.UnblockContactErrorDTO
import com.naposystems.napoleonchat.dto.contacts.unblockContact.UnblockContactResDTO
import com.naposystems.napoleonchat.dto.muteConversation.MuteConversationErrorDTO
import com.naposystems.napoleonchat.dto.muteConversation.MuteConversationReqDTO
import com.naposystems.napoleonchat.dto.muteConversation.MuteConversationResDTO
import com.naposystems.napoleonchat.entity.Contact
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.utility.sharedViewModels.contact.IContractShareContact
import com.naposystems.napoleonchat.webService.NapoleonApi
import com.squareup.moshi.Moshi
import retrofit2.Response
import javax.inject.Inject

class ShareContactRepository
@Inject constructor(
    private val napoleonApi: NapoleonApi,
    private val contactLocalDataSource: ContactDataSource,
    private val messageLocalDataSource: MessageLocalDataSource
) : IContractShareContact.Repository {

    private val moshi: Moshi by lazy {
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
        RxBus.publish(RxEvent.DeleteChannel(contact))
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