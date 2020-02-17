package com.naposystems.pepito.repository.contacts

import androidx.lifecycle.LiveData
import com.naposystems.pepito.db.dao.contact.ContactDataSource
import com.naposystems.pepito.dto.contacts.ContactResDTO
import com.naposystems.pepito.dto.contacts.blockedContact.BlockedContactResDTO
import com.naposystems.pepito.dto.contacts.deleteContact.DeleteContactErrorDTO
import com.naposystems.pepito.dto.contacts.deleteContact.DeleteContactResDTO
import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.ui.contacts.IContractContacts
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.webService.NapoleonApi
import com.squareup.moshi.Moshi
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

class ContactsRepository @Inject constructor(
    private val napoleonApi: NapoleonApi,
    private val contactLocalDataSource: ContactDataSource,
    private val sharedPreferencesManager: SharedPreferencesManager
) :
    IContractContacts.Repository {

    private val moshi by lazy {
        Moshi.Builder().build()
    }

    override suspend fun getLocalContacts(): LiveData<List<Contact>> {
        return contactLocalDataSource.getContacts()
    }

    override suspend fun getRemoteContacts() {
        try {

            val response = napoleonApi.getContactsByState(Constants.FriendShipState.ACTIVE.state)

            if (response.isSuccessful) {

                val contactResDTO = response.body()!!

                val contacts = ContactResDTO.toEntityList(contactResDTO.contacts)

                contactLocalDataSource.insertContactList(contacts)
            } else {
                Timber.e(response.errorBody()!!.string())
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override suspend fun sendBlockedContact(contact: Contact): Response<BlockedContactResDTO> {
        return napoleonApi.putBlockContact(contact.id.toString())
    }

    override suspend fun blockContactLocal(contactId: Int) {
        contactLocalDataSource.blockContact(contactId)
    }

    override suspend fun sendDeleteContact(contact: Contact): Response<DeleteContactResDTO> {
        return napoleonApi.sendDeleteContact(contact.id.toString())
    }

    override suspend fun deleteContactLocal(contact: Contact) {
        contactLocalDataSource.deleteContact(contact)
    }

    override fun getDefaultDeleteError(response: Response<DeleteContactResDTO>): List<String> {
        val adapter = moshi.adapter(DeleteContactErrorDTO::class.java)
        val updateUserInfoError = adapter.fromJson(response.errorBody()!!.string())

        return arrayListOf(updateUserInfoError!!.error)
    }

    override fun getDefaultBlockedError(response: Response<BlockedContactResDTO>): List<String> {
        val adapter = moshi.adapter(DeleteContactErrorDTO::class.java)
        val updateUserInfoError = adapter.fromJson(response.errorBody()!!.string())

        return arrayListOf(updateUserInfoError!!.error)
    }
}