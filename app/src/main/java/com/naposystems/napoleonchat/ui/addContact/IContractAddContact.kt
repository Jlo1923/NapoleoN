package com.naposystems.napoleonchat.ui.addContact

import com.naposystems.napoleonchat.model.FriendShipRequest
import com.naposystems.napoleonchat.model.FriendShipRequestAdapterType
import com.naposystems.napoleonchat.model.addContact.Contact
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.source.local.entity.UserEntity
import com.naposystems.napoleonchat.source.remote.dto.addContact.FriendshipRequestPutResDTO
import com.naposystems.napoleonchat.source.remote.dto.addContact.FriendshipRequestResDTO
import com.naposystems.napoleonchat.source.remote.dto.addContact.FriendshipRequestsResDTO
import com.naposystems.napoleonchat.source.remote.dto.contacts.ContactResDTO
import retrofit2.Response

interface IContractAddContact {

    interface ViewModel {
        fun searchContact(query: String)
        fun resetContacts()
        fun getUsers() : List<Any>?
        fun getSearchOpened() : Boolean?
        fun setSearchOpened()
        fun getRequestSend() : List<FriendShipRequestAdapterType>?
        fun sendFriendshipRequest(contact: Contact)
        fun getFriendshipRequests()
        fun acceptOrRefuseRequest(contact: Contact, state:Boolean): FriendShipRequest
        fun validateIfExistsOffer()
        fun getContact(contact: Contact): ContactEntity?
    }

    interface Repository {
        suspend fun searchContact(query: String): Response<List<ContactResDTO>>
        suspend fun sendFriendshipRequest(contact: Contact): Response<FriendshipRequestResDTO>
        suspend fun getFriendshipRequest(): Response<FriendshipRequestsResDTO>
        fun getError(response: Response<FriendshipRequestPutResDTO>): String
        suspend fun getUser(): UserEntity
        fun getContact(idContact:Int): ContactEntity?
    }
}