package com.naposystems.napoleonchat.repository.addContact

import com.naposystems.napoleonchat.db.dao.user.UserLocalDataSource
import com.naposystems.napoleonchat.dto.addContact.*
import com.naposystems.napoleonchat.dto.contacts.ContactResDTO
import com.naposystems.napoleonchat.entity.Contact
import com.naposystems.napoleonchat.entity.User
import com.naposystems.napoleonchat.ui.addContact.IContractAddContact
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.webService.NapoleonApi
import com.squareup.moshi.Moshi
import retrofit2.Response
import javax.inject.Inject

class AddContactRepository @Inject constructor(
    private val moshi: Moshi,
    private val napoleonApi: NapoleonApi,
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

    override fun getError(response: Response<FriendshipRequestPutResDTO>): String {

        val adapter = moshi.adapter(FriendshipRequestPutErrorDTO::class.java)

        val updateUserInfoError = adapter.fromJson(response.errorBody()!!.string())

        return updateUserInfoError!!.error
    }

    override suspend fun getUser(): User {

        return userLocalDataSource.getMyUser()
//
//        return userLocalDataSource.getUser(
//            sharedPreferencesManager.getString(
//                Constants.SharedPreferences.PREF_FIREBASE_ID,
//                ""
//            )
//        )
    }
}