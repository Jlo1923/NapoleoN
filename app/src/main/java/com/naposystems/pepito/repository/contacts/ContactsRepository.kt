package com.naposystems.pepito.repository.contacts

import com.naposystems.pepito.dto.contacts.ContactsResDTO
import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.ui.contacts.IContractContacts
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.webService.NapoleonApi
import timber.log.Timber
import javax.inject.Inject

class ContactsRepository @Inject constructor(private val napoleonApi: NapoleonApi) :
    IContractContacts.Repository {

    override suspend fun getContacts(): List<Contact> {
        val contacts: MutableList<Contact> = arrayListOf()

        try {
            val response =
                napoleonApi.getFriendShipSearch(Constants.FriendShipState.ACTIVE.state)

            if (response.isSuccessful) {

                contacts.addAll(ContactsResDTO.toEntityList(response.body()!!))

            } else {
                Timber.e(response.errorBody()!!.string())
            }
        } catch (e: Exception) {
            Timber.e(e)
        }

        return contacts
    }
}