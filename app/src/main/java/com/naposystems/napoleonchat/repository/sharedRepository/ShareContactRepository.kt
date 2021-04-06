package com.naposystems.napoleonchat.repository.sharedRepository

import com.naposystems.napoleonchat.source.local.datasource.contact.ContactLocalDataSource
import com.naposystems.napoleonchat.source.local.datasource.message.MessageLocalDataSourceImp
import com.naposystems.napoleonchat.source.remote.dto.contacts.blockedContact.BlockedContactResDTO
import com.naposystems.napoleonchat.source.remote.dto.contacts.deleteContact.DeleteContactErrorDTO
import com.naposystems.napoleonchat.source.remote.dto.contacts.deleteContact.DeleteContactResDTO
import com.naposystems.napoleonchat.source.remote.dto.contacts.unblockContact.UnblockContactErrorDTO
import com.naposystems.napoleonchat.source.remote.dto.contacts.unblockContact.UnblockContactResDTO
import com.naposystems.napoleonchat.source.remote.dto.muteConversation.MuteConversationErrorDTO
import com.naposystems.napoleonchat.source.remote.dto.muteConversation.MuteConversationReqDTO
import com.naposystems.napoleonchat.source.remote.dto.muteConversation.MuteConversationResDTO
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.utility.sharedViewModels.contact.IContractShareContact
import com.naposystems.napoleonchat.source.remote.api.NapoleonApi
import com.squareup.moshi.Moshi
import retrofit2.Response
import javax.inject.Inject

class ShareContactRepository
@Inject constructor(
    private val napoleonApi: NapoleonApi,
    private val contactLocalDataSource: ContactLocalDataSource,
    private val messageLocalDataSourceImp: MessageLocalDataSourceImp
) : IContractShareContact.Repository {

    private val moshi: Moshi by lazy {
        Moshi.Builder().build()
    }

    override suspend fun sendBlockedContact(contact: ContactEntity): Response<BlockedContactResDTO> {
        return napoleonApi.putBlockContact(contact.id.toString())
    }

    override suspend fun blockContactLocal(contact: ContactEntity) {
        contactLocalDataSource.blockContact(contact.id)
        deleteConversation(contact.id)
    }

    override suspend fun unblockContact(contactId: Int): Response<UnblockContactResDTO> {
        return napoleonApi.putUnblockContact(contactId.toString())
    }

    override suspend fun unblockContactLocal(contactId: Int) {
        contactLocalDataSource.unblockContact(contactId)
    }

    override suspend fun sendDeleteContact(contact: ContactEntity): Response<DeleteContactResDTO> {
        return napoleonApi.sendDeleteContact(contact.id.toString())
    }

    override suspend fun deleteContactLocal(contact: ContactEntity) {
        RxBus.publish(RxEvent.DeleteChannel(contact))
        contactLocalDataSource.deleteContact(contact)
    }

    override suspend fun deleteConversation(contactId: Int) {
        messageLocalDataSourceImp.deleteMessages(contactId)
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