package com.naposystems.napoleonchat.repository.addContact

import com.naposystems.napoleonchat.db.dao.contact.ContactDataSource
import com.naposystems.napoleonchat.db.dao.message.MessageDataSource
import com.naposystems.napoleonchat.db.dao.user.UserLocalDataSource
import com.naposystems.napoleonchat.dto.addContact.*
import com.naposystems.napoleonchat.dto.contacts.ContactResDTO
import com.naposystems.napoleonchat.dto.conversation.message.MessageReqDTO
import com.naposystems.napoleonchat.dto.conversation.message.MessageResDTO
import com.naposystems.napoleonchat.entity.Contact
import com.naposystems.napoleonchat.entity.User
import com.naposystems.napoleonchat.entity.addContact.FriendShipRequest
import com.naposystems.napoleonchat.entity.message.Message
import com.naposystems.napoleonchat.ui.addContact.IContractAddContact
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Constants.FriendshipRequestPutAction
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.webService.NapoleonApi
import com.squareup.moshi.Moshi
import retrofit2.Response
import javax.inject.Inject

class AddContactRepository @Inject constructor(
    private val napoleonApi: NapoleonApi,
    /*private val contactLocalDataSource: ContactDataSource,
    private val messageLocalDataSource: MessageDataSource,*/
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

    /*override suspend fun refuseFriendshipRequest(friendShipRequest: FriendShipRequest): Response<FriendshipRequestPutResDTO> {
        val request = FriendshipRequestPutReqDTO(FriendshipRequestPutAction.REFUSE.action)
        return napoleonApi.putFriendshipRequest(friendShipRequest.id.toString(), request)
    }

    override suspend fun acceptFriendshipRequest(friendShipRequest: FriendShipRequest): Response<FriendshipRequestPutResDTO> {
        val request = FriendshipRequestPutReqDTO(FriendshipRequestPutAction.ACCEPT.action)
        return napoleonApi.putFriendshipRequest(friendShipRequest.id.toString(), request)
    }

    override suspend fun addContact(friendShipRequest: FriendShipRequest) {
        contactLocalDataSource.insertContact(friendShipRequest.contact)
    }

    override suspend fun sendNewContactMessage(messageReqDTO: MessageReqDTO): Response<MessageResDTO> {
        return napoleonApi.sendMessage(messageReqDTO)
    }

    override fun insertMessage(message: Message): Long {
        return messageLocalDataSource.insertMessage(message)
    }*/

    override fun getError(response: Response<FriendshipRequestPutResDTO>): String {
        val moshi = Moshi.Builder().build()
        val adapter = moshi.adapter(FriendshipRequestPutErrorDTO::class.java)

        val updateUserInfoError = adapter.fromJson(response.errorBody()!!.string())

        return updateUserInfoError!!.error
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