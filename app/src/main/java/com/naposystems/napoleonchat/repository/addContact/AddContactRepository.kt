package com.naposystems.napoleonchat.repository.addContact

import com.naposystems.napoleonchat.model.addContact.Contact
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.source.local.entity.UserEntity
import com.naposystems.napoleonchat.source.remote.dto.addContact.FriendshipRequestPutResDTO
import com.naposystems.napoleonchat.source.remote.dto.addContact.FriendshipRequestResDTO
import com.naposystems.napoleonchat.source.remote.dto.addContact.FriendshipRequestsResDTO
import com.naposystems.napoleonchat.source.remote.dto.contacts.ContactResDTO
import retrofit2.Response

interface AddContactRepository {
    suspend fun searchContact(query: String): Response<List<ContactResDTO>>
    suspend fun sendFriendshipRequest(contact: Contact): Response<FriendshipRequestResDTO>
    suspend fun getFriendshipRequest(): Response<FriendshipRequestsResDTO>
    fun getError(response: Response<FriendshipRequestPutResDTO>): String
    suspend fun getUser(): UserEntity
    fun getContact(idContact: Int): ContactEntity?
}