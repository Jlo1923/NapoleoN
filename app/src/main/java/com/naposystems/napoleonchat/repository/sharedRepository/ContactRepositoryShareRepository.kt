package com.naposystems.napoleonchat.repository.sharedRepository

import com.naposystems.napoleonchat.db.dao.contact.ContactDataSource
import com.naposystems.napoleonchat.db.dao.message.MessageDataSource
import com.naposystems.napoleonchat.dto.contacts.ContactResDTO
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.sharedViewModels.contactRepository.IContractContactRepositoryShare
import com.naposystems.napoleonchat.webService.NapoleonApi
import timber.log.Timber
import javax.inject.Inject

class ContactRepositoryShareRepository @Inject constructor(
    private val napoleonApi: NapoleonApi,
    private val contactLocalDataSource: ContactDataSource,
    private val messageLocalDataSource: MessageDataSource
) : IContractContactRepositoryShare.Repository {

    override suspend fun getContacts(): Boolean {
        return try {
            val response = napoleonApi.getContactsByState(Constants.FriendShipState.ACTIVE.state)

            if (response.isSuccessful) {

                val contactResDTO = response.body()!!

                val contacts = ContactResDTO.toEntityList(contactResDTO.contacts)

                val contactsToDelete = contactLocalDataSource.insertOrUpdateContactList(contacts)

                if (contactsToDelete.isNotEmpty()) {

                    contactsToDelete.forEach { contact ->
                        messageLocalDataSource.deleteMessageByType(
                            contact.id,
                            Constants.MessageType.NEW_CONTACT.type
                        )
                        contactLocalDataSource.deleteContact(contact)
                    }
                }

                true
            } else {
                Timber.e(response.errorBody()!!.string())
                false
            }
        } catch (e: Exception) {
            Timber.e(e)
            true
        }
    }
}