package com.naposystems.napoleonchat.utility.sharedViewModels.friendShipAction

import com.naposystems.napoleonchat.model.FriendShipRequest
import com.naposystems.napoleonchat.source.local.datasource.contact.ContactLocalDataSource
import com.naposystems.napoleonchat.source.local.datasource.message.MessageLocalDataSource
import com.naposystems.napoleonchat.source.local.entity.MessageEntity
import com.naposystems.napoleonchat.source.remote.api.NapoleonApi
import com.naposystems.napoleonchat.source.remote.dto.addContact.FriendshipRequestPutErrorDTO
import com.naposystems.napoleonchat.source.remote.dto.addContact.FriendshipRequestPutReqDTO
import com.naposystems.napoleonchat.source.remote.dto.addContact.FriendshipRequestPutResDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.message.MessageReqDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.message.MessageResDTO
import com.naposystems.napoleonchat.utility.Constants
import com.squareup.moshi.Moshi
import retrofit2.Response
import javax.inject.Inject

class FriendShipActionSharedRepositoryImp @Inject constructor(
    private val napoleonApi: NapoleonApi,
    private val contactLocalDataSource: ContactLocalDataSource,
    private val messageLocalDataSource: MessageLocalDataSource
) : FriendShipActionSharedRepository {

    private val moshi: Moshi by lazy {
        Moshi.Builder().build()
    }

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

    override suspend fun insertMessage(messageEntity: MessageEntity): Long {
        return messageLocalDataSource.insertMessage(messageEntity)
    }
}