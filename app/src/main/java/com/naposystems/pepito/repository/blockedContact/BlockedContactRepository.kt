package com.naposystems.pepito.repository.blockedContact

import androidx.lifecycle.LiveData
import com.naposystems.pepito.db.dao.contact.ContactDataSource
import com.naposystems.pepito.dto.contacts.unblockContact.UnblockContactResDTO
import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.ui.blockedContacts.IContractBlockedContact
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.webService.NapoleonApi
import retrofit2.Response
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

    override suspend fun unblockContact(contact: Contact): Response<UnblockContactResDTO> {
        return napoleonApi.putUnblockContact(contact.id.toString())
    }

    override suspend fun unblockContactLocal(contactId: Int) {
        contactLocalDataSource.unblockContact(contactId)
    }
}