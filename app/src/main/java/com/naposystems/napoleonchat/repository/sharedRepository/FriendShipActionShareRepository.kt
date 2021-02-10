package com.naposystems.napoleonchat.repository.sharedRepository

import com.naposystems.napoleonchat.db.dao.contact.ContactDataSource
import com.naposystems.napoleonchat.db.dao.message.MessageDataSource
import com.naposystems.napoleonchat.dto.addContact.FriendshipRequestPutErrorDTO
import com.naposystems.napoleonchat.dto.addContact.FriendshipRequestPutReqDTO
import com.naposystems.napoleonchat.dto.addContact.FriendshipRequestPutResDTO
import com.naposystems.napoleonchat.dto.conversation.message.MessageReqDTO
import com.naposystems.napoleonchat.dto.conversation.message.MessageResDTO
import com.naposystems.napoleonchat.entity.addContact.FriendShipRequest
import com.naposystems.napoleonchat.entity.message.Message
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.sharedViewModels.friendShipAction.IContractFriendShipAction
import com.naposystems.napoleonchat.webService.NapoleonApi
import com.squareup.moshi.Moshi
import retrofit2.Response
import javax.inject.Inject

class FriendShipActionShareRepository @Inject constructor(
    private val moshi: Moshi,
    private val napoleonApi: NapoleonApi,
    private val contactLocalDataSource: ContactDataSource,
    private val messageLocalDataSource: MessageDataSource
) : IContractFriendShipAction.Repository {

    override suspend fun refuseFriendshipRequest(friendShipRequest: FriendShipRequest): Response<FriendshipRequestPutResDTO> {
        val request = FriendshipRequestPutReqDTO(Constants.FriendshipRequestPutAction.REFUSE.action)
        return napoleonApi.putFriendshipRequest(friendShipRequest.id.toString(), request)
    }

    override suspend fun acceptFriendshipRequest(friendShipRequest: FriendShipRequest): Response<FriendshipRequestPutResDTO> {
        val request = FriendshipRequestPutReqDTO(Constants.FriendshipRequestPutAction.ACCEPT.action)
        return napoleonApi.putFriendshipRequest(friendShipRequest.id.toString(), request)
    }

    override suspend fun cancelFriendshipRequest(friendShipRequest: FriendShipRequest): Response<FriendshipRequestPutResDTO> {
        val request = FriendshipRequestPutReqDTO(Constants.FriendshipRequestPutAction.CANCEL.action)
        return napoleonApi.putFriendshipRequest(friendShipRequest.id.toString(), request)
    }

    override fun getError(response: Response<FriendshipRequestPutResDTO>): String {

        val adapter = moshi.adapter(FriendshipRequestPutErrorDTO::class.java)

        val updateUserInfoError = adapter.fromJson(response.errorBody()!!.string())

        return updateUserInfoError!!.error
    }

    override suspend fun addContact(friendShipRequest: FriendShipRequest) {
        contactLocalDataSource.insertContact(friendShipRequest.contact)
    }

    override suspend fun sendNewContactMessage(messageReqDTO: MessageReqDTO): Response<MessageResDTO> {
        return napoleonApi.sendMessage(messageReqDTO)
    }

    override fun insertMessage(message: Message): Long {
        return messageLocalDataSource.insertMessage(message)
    }
}