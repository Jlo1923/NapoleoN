package com.naposystems.pepito.ui.addContact

import com.naposystems.pepito.dto.addContact.FriendshipRequestPutResDTO
import com.naposystems.pepito.dto.addContact.FriendshipRequestResDTO
import com.naposystems.pepito.dto.addContact.FriendshipRequestsResDTO
import com.naposystems.pepito.dto.contacts.ContactResDTO
import com.naposystems.pepito.dto.conversation.message.MessageReqDTO
import com.naposystems.pepito.dto.conversation.message.MessageResDTO
import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.entity.User
import com.naposystems.pepito.entity.addContact.FriendShipRequest
import com.naposystems.pepito.entity.addContact.FriendShipRequestAdapterType
import com.naposystems.pepito.entity.message.Message
import retrofit2.Response

interface IContractAddContact {

    interface ViewModel {
        fun searchContact(query: String)
        fun resetContacts()
        fun getUsers() : List<Contact>?
        fun getSearchOpened() : Boolean?
        fun setSearchOpened()
        fun getRequestSend() : List<FriendShipRequestAdapterType>?
        fun sendFriendshipRequest(contact: Contact)
        fun getFriendshipRequests()
        fun cancelFriendshipRequest(friendShipRequest: FriendShipRequest)
        fun refuseFriendshipRequest(friendShipRequest: FriendShipRequest)
        fun acceptFriendshipRequest(friendShipRequest: FriendShipRequest)
    }

    interface Repository {
        suspend fun searchContact(query: String): Response<List<ContactResDTO>>
        suspend fun sendFriendshipRequest(contact: Contact): Response<FriendshipRequestResDTO>
        suspend fun getFriendshipRequest(): Response<FriendshipRequestsResDTO>
        suspend fun cancelFriendshipRequest(friendShipRequest: FriendShipRequest): Response<FriendshipRequestPutResDTO>
        suspend fun refuseFriendshipRequest(friendShipRequest: FriendShipRequest): Response<FriendshipRequestPutResDTO>
        suspend fun acceptFriendshipRequest(friendShipRequest: FriendShipRequest): Response<FriendshipRequestPutResDTO>
        fun getError(response: Response<FriendshipRequestPutResDTO>): String
        suspend fun addContact(friendShipRequest: FriendShipRequest)
        suspend fun sendNewContactMessage(messageReqDTO: MessageReqDTO): Response<MessageResDTO>
        fun insertMessage(message: Message): Long
        suspend fun getUser(): User
    }
}