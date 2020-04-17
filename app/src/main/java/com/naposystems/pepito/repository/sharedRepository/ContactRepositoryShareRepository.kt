package com.naposystems.pepito.repository.sharedRepository

import com.naposystems.pepito.db.dao.contact.ContactDataSource
import com.naposystems.pepito.dto.contacts.ContactResDTO
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.sharedViewModels.contactRepository.IContractContactRepositoryShare
import com.naposystems.pepito.webService.NapoleonApi
import timber.log.Timber
import javax.inject.Inject

class ContactRepositoryShareRepository @Inject constructor(
    private val napoleonApi: NapoleonApi,
    private val contactLocalDataSource: ContactDataSource
) : IContractContactRepositoryShare.Repository {

    override suspend fun getContacts() {
        try {
            val response = napoleonApi.getContactsByState(Constants.FriendShipState.ACTIVE.state)

            if (response.isSuccessful) {

                val contactResDTO = response.body()!!

                val contacts = ContactResDTO.toEntityList(contactResDTO.contacts)

                contactLocalDataSource.insertOrUpdateContactList(contacts)
            } else {
                Timber.e(response.errorBody()!!.string())
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
}