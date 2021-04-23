package com.naposystems.napoleonchat.utility.sharedViewModels.contact

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.source.remote.dto.muteConversation.MuteConversationReqDTO
import com.naposystems.napoleonchat.utility.Utils
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class ContactSharedViewModel
@Inject constructor(
    private val repository: ContactSharedRepository
) : ViewModel() {

    private val _contactsWasLoaded = MutableLiveData<Boolean>()
    val contactsWasLoaded: LiveData<Boolean>
        get() = _contactsWasLoaded

    private val _webServiceErrors = MutableLiveData<List<String>>()
    val webServiceErrors: LiveData<List<String>>
        get() = _webServiceErrors

    private val _muteConversationWsError = MutableLiveData<List<String>>()
    val muteConversationWsError: LiveData<List<String>>
        get() = _muteConversationWsError

    private val _conversationDeleted = MutableLiveData<Boolean>()
    val conversationDeleted: LiveData<Boolean>
        get() = _conversationDeleted

    fun getContacts(state: String, location: Int) {
        viewModelScope.launch {
            try {
                _contactsWasLoaded.value = repository.getContacts(state, location)
            } catch (ex: Exception) {
                Timber.e(ex)
            }
        }
    }

    fun sendBlockedContact(contact: ContactEntity) {
        viewModelScope.launch {
            try {
                val response = repository.sendBlockedContact(contact)

                if (response.isSuccessful) {
                    contact.statusBlocked = true
                    contact.stateNotification = false
                    repository.blockContactLocal(contact)
                } else {
                    _webServiceErrors.value = repository.getDefaultBlockedError(response)
                }
            } catch (ex: Exception) {
                Timber.e(ex)
            }
        }
    }

    fun unblockContact(contactId: Int) {
        viewModelScope.launch {
            try {
                val response = repository.unblockContact(contactId)

                if (response.isSuccessful) {
                    repository.unblockContactLocal(contactId)
                } else {
                    _webServiceErrors.value = repository.getDefaultUnblockError(response)
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    fun sendDeleteContact(contact: ContactEntity) {
        viewModelScope.launch {
            try {
                val response = repository.sendDeleteContact(contact)

                if (response.isSuccessful) {
                    repository.deleteContactLocal(contact)
                } else {
                    _webServiceErrors.value = repository.getDefaultDeleteError(response)
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    fun deleteConversation(contactId: Int) {
        viewModelScope.launch {
            repository.deleteConversation(contactId)
            _conversationDeleted.value = true
        }
    }

    fun muteConversation(contactId: Int, contactSilenced: Boolean) {
        viewModelScope.launch {
            try {
                val response = repository.muteConversation(contactId, MuteConversationReqDTO())

                if (response.isSuccessful) {
                    repository.muteConversationLocal(
                        contactId,
                        Utils.convertBooleanToInvertedInt(contactSilenced)
                    )
                } else {
                    _muteConversationWsError.value = repository.muteError(response)
                }
            } catch (ex: Exception) {
                Timber.e(ex)
            }
        }
    }

}