package com.naposystems.pepito.repository.addContact

import com.naposystems.pepito.db.dao.contact.ContactDataSource
import com.naposystems.pepito.db.dao.message.MessageDataSource
import com.naposystems.pepito.db.dao.user.UserDataSource
import com.naposystems.pepito.db.dao.user.UserLocalDataSource
import com.naposystems.pepito.dto.addContact.*
import com.naposystems.pepito.dto.contacts.ContactResDTO
import com.naposystems.pepito.dto.conversation.message.MessageReqDTO
import com.naposystems.pepito.dto.conversation.message.MessageResDTO
import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.entity.User
import com.naposystems.pepito.entity.addContact.FriendShipRequest
import com.naposystems.pepito.entity.message.Message
import com.naposystems.pepito.ui.addContact.IContractAddContact
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.Constants.FriendshipRequestPutAction
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.webService.NapoleonApi
import com.squareup.moshi.Moshi
import retrofit2.Response
import javax.inject.Inject

class AddContactRepository @Inject constructor(
    private val napoleonApi: NapoleonApi,
    private val contactLocalDataSource: ContactDataSource,
    private val messageLocalDataSource: MessageDataSource,
    private val userLocalDataSource: UserLocalDataSource,
    private val sharedPreferencesManager: SharedPreferencesManager
) :
    IContractAddContact.Repository {

    override suspend fun searchContact(query: String): Response<List<ContactResDTO>> {
        return napoleonApi.searchUser(query)
    }

    override suspend fun sendFriendshipRequest(contact: Contact): Response<FriendshipRequestResDTO> {
        val friendshipRequestReqDTO = FriendshipRequestReqDTO(
            contact.id
        )

        return napoleonApi.sendFriendshipRequest(friendshipRequestReqDTO)
    }

    override suspend fun getFriendshipRequest(): Response<FriendshipRequestsResDTO> {
        return napoleonApi.getFriendshipRequests()
    }

    override suspend fun cancelFriendshipRequest(friendShipRequest: FriendShipRequest): Response<FriendshipRequestPutResDTO> {
        val request = FriendshipRequestPutReqDTO(FriendshipRequestPutAction.CANCEL.action)
        return napoleonApi.putFriendshipRequest(friendShipRequest.id.toString(), request)
    }

    override suspend fun refuseFriendshipRequest(friendShipRequest: FriendShipRequest): Response<FriendshipRequestPutResDTO> {
        val request = FriendshipRequestPutReqDTO(FriendshipRequestPutAction.REFUSE.action)
        return napoleonApi.putFriendshipRequest(friendShipRequest.id.toString(), request)
    }

    override suspend fun acceptFriendshipRequest(friendShipRequest: FriendShipRequest): Response<FriendshipRequestPutResDTO> {
        val request = FriendshipRequestPutReqDTO(FriendshipRequestPutAction.ACCEPT.action)
        return napoleonApi.putFriendshipRequest(friendShipRequest.id.toString(), request)
    }

    override fun getError(response: Response<FriendshipRequestPutResDTO>): String {
        val moshi = Moshi.Builder().build()
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

    override suspend fun getUser(): User {
        return userLocalDataSource.getUser(
            sharedPreferencesManager.getString(
                Constants.SharedPreferences.PREF_FIREBASE_ID,
                ""
            )
        )
    }
}