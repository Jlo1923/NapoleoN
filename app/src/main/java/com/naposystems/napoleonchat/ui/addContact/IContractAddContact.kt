package com.naposystems.napoleonchat.ui.addContact

import com.naposystems.napoleonchat.dto.addContact.FriendshipRequestPutResDTO
import com.naposystems.napoleonchat.dto.addContact.FriendshipRequestResDTO
import com.naposystems.napoleonchat.dto.addContact.FriendshipRequestsResDTO
import com.naposystems.napoleonchat.dto.contacts.ContactResDTO
import com.naposystems.napoleonchat.entity.Contact
import com.naposystems.napoleonchat.entity.User
import com.naposystems.napoleonchat.entity.addContact.FriendShipRequestAdapterType
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
    }

    interface Repository {
        suspend fun searchContact(query: String): Response<List<ContactResDTO>>
        suspend fun sendFriendshipRequest(contact: Contact): Response<FriendshipRequestResDTO>
        suspend fun getFriendshipRequest(): Response<FriendshipRequestsResDTO>
        fun getError(response: Response<FriendshipRequestPutResDTO>): String
        suspend fun getUser(): User
    }
}