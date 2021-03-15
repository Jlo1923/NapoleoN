package com.naposystems.napoleonchat.repository.sharedRepository

import com.naposystems.napoleonchat.source.local.datasource.contact.ContactLocalDataSource
import com.naposystems.napoleonchat.source.local.datasource.message.MessageLocalDataSource
import com.naposystems.napoleonchat.source.remote.dto.contacts.ContactResDTO
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.sharedViewModels.contactRepository.IContractContactRepositoryShare
import com.naposystems.napoleonchat.source.remote.api.NapoleonApi
import timber.log.Timber
import javax.inject.Inject

class ContactRepositoryShareRepository
@Inject constructor(
    private val napoleonApi: NapoleonApi,
    private val contactLocalDataSource: ContactLocalDataSource,
    private val messageLocalDataSource: MessageLocalDataSource
) : IContractContactRepositoryShare.Repository {

    override suspend fun getContacts(state : String, location : Int): Boolean {
        return try {
            val response = napoleonApi.getContactsByState(state)

            if (response.isSuccessful) {

                val contactResDTO = response.body()!!

                val contacts = if (state == Constants.FriendShipState.BLOCKED.state)
                    ContactResDTO.toEntityList(contactResDTO.contacts, true)
                else
                    ContactResDTO.toEntityList(contactResDTO.contacts)

                val contactsToDelete = contactLocalDataSource.insertOrUpdateContactList(
                    contacts, location
                )

                if (contactsToDelete.isNotEmpty() && location == Constants.LocationGetContact.OTHER.location) {
                    contactsToDelete.forEach { contact ->
                        messageLocalDataSource.deleteMessageByType(
                            contact.id,
                            Constants.MessageType.NEW_CONTACT.type
                        )

                        RxBus.publish(RxEvent.DeleteChannel(contact))

                        contactLocalDataSource.deleteContact(contact)
                        Timber.d("*TestDelete: ContactDelete ${contact.getNickName()}")

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