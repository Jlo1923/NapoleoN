package com.naposystems.napoleonchat.repository.addContact

import com.naposystems.napoleonchat.model.addContact.Contact
import com.naposystems.napoleonchat.source.local.datasource.contact.ContactLocalDataSourceImp
import com.naposystems.napoleonchat.source.local.datasource.user.UserLocalDataSourceImp
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.source.local.entity.UserEntity
import com.naposystems.napoleonchat.source.remote.api.NapoleonApi
import com.naposystems.napoleonchat.source.remote.dto.addContact.*
import com.naposystems.napoleonchat.source.remote.dto.contacts.ContactResDTO
import com.squareup.moshi.Moshi
import retrofit2.Response
import javax.inject.Inject

class AddContactRepositoryImp @Inject constructor(
    private val napoleonApi: NapoleonApi,
    private val userLocalDataSourceImp: UserLocalDataSourceImp,
    private val contactLocalDataSourceImp: ContactLocalDataSourceImp
) :
    AddContactRepository {

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

    override fun getError(response: Response<FriendshipRequestPutResDTO>): String {

        val moshi = Moshi.Builder().build()

        val adapter = moshi.adapter(FriendshipRequestPutErrorDTO::class.java)

        val updateUserInfoError = adapter.fromJson(response.errorBody()!!.string())

        return updateUserInfoError!!.error
    }

    override suspend fun getUser(): UserEntity {
        return userLocalDataSourceImp.getMyUser()
    }

    override fun getContact(idContact: Int): ContactEntity? {
        return contactLocalDataSourceImp.getContactById(idContact)
    }
}