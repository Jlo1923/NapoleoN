package com.naposystems.pepito.repository.contacts

import androidx.lifecycle.LiveData
import com.naposystems.pepito.db.dao.contact.ContactDataSource
import com.naposystems.pepito.dto.contacts.ContactResDTO
import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.ui.contacts.IContractContacts
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.webService.NapoleonApi
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

class ContactsRepository @Inject constructor(
    private val napoleonApi: NapoleonApi,
    private val contactLocalDataSource: ContactDataSource,
    private val sharedPreferencesManager: SharedPreferencesManager
) :
    IContractContacts.Repository {

    override suspend fun getLocalContacts(): LiveData<List<Contact>> {
        return contactLocalDataSource.getContacts()
    }

    override suspend fun getRemoteContacts() {
        try {

            val response = napoleonApi.getContactsByState(Constants.FriendShipState.ACTIVE.state)

            if (response.isSuccessful) {

                val contactResDTO = response.body()!!

                val contacts = ContactResDTO.toEntityList(contactResDTO.contacts)

                contactLocalDataSource.insertContactList(contacts, true)

                if (contactResDTO.date.isNotEmpty()) {
                    sharedPreferencesManager.putString(
                        Constants.SharedPreferences.PREF_CONTACTS_UPDATE_DATE,
                        contactResDTO.date
                    )
                }
            } else {
                Timber.e(response.errorBody()!!.string())
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

}