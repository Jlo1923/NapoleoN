package com.naposystems.napoleonchat.repository.blockedContact

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.db.dao.contact.ContactDataSource
import com.naposystems.napoleonchat.entity.Contact
import com.naposystems.napoleonchat.ui.blockedContacts.IContractBlockedContact
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.webService.NapoleonApi
import timber.log.Timber

class BlockedContactRepository constructor(
    private val napoleonApi: NapoleonApi,
    private val contactLocalDataSource: ContactDataSource
) : IContractBlockedContact.Repository {

    override suspend fun getRemoteBlockedContacts() {
        try {
            val response = napoleonApi.getContactsByState(Constants.FriendShipState.BLOCKED.state)

            if (response.isSuccessful) {
                for (contact in response.body()!!.contacts) {
                    contactLocalDataSource.blockContact(contact.id)
                }
            } else {
                Timber.e(response.errorBody()!!.string())
            }

        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override suspend fun getBlockedContactsLocal(): LiveData<List<Contact>> {
        return contactLocalDataSource.getBlockedContacts()
    }
}