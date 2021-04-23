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
    private val sharedRepository: ContactSharedRepository
) : ViewModel() {

    private val _webServiceErrors = MutableLiveData<List<String>>()
    val webServiceErrors: LiveData<List<String>>
        get() = _webServiceErrors

    private val _muteConversationWsError = MutableLiveData<List<String>>()
    val muteConversationWsError: LiveData<List<String>>
        get() = _muteConversationWsError

    private val _conversationDeleted = MutableLiveData<Boolean>()
    val conversationDeleted: LiveData<Boolean>
        get() = _conversationDeleted

    fun sendBlockedContact(contact: ContactEntity) {
        viewModelScope.launch {
            try {
                val response = sharedRepository.sendBlockedContact(contact)

                if (response.isSuccessful) {
                    contact.statusBlocked = true
                    contact.stateNotification = false
                    sharedRepository.blockContactLocal(contact)
                } else {
                    _webServiceErrors.value = sharedRepository.getDefaultBlockedError(response)
                }
            } catch (ex: Exception) {
                Timber.e(ex)
            }
        }
    }

    fun unblockContact(contactId: Int) {
        viewModelScope.launch {
            try {
                val response = sharedRepository.unblockContact(contactId)

                if (response.isSuccessful) {
                    sharedRepository.unblockContactLocal(contactId)
                } else {
                    _webServiceErrors.value = sharedRepository.getDefaultUnblockError(response)
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    fun sendDeleteContact(contact: ContactEntity) {
        viewModelScope.launch {
            try {
                val response = sharedRepository.sendDeleteContact(contact)

                if (response.isSuccessful) {
                    sharedRepository.deleteContactLocal(contact)
                } else {
                    _webServiceErrors.value = sharedRepository.getDefaultDeleteError(response)
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    fun deleteConversation(contactId: Int) {
        viewModelScope.launch {
            sharedRepository.deleteConversation(contactId)
            _conversationDeleted.value = true
        }
    }

    fun muteConversation(contactId: Int, contactSilenced: Boolean) {
        viewModelScope.launch {
            try {
                val response = sharedRepository.muteConversation(contactId, MuteConversationReqDTO())

                if (response.isSuccessful) {
                    sharedRepository.muteConversationLocal(
                        contactId,
                        Utils.convertBooleanToInvertedInt(contactSilenced)
                    )
                } else {
                    _muteConversationWsError.value = sharedRepository.muteError(response)
                }
            } catch (ex: Exception) {
                Timber.e(ex)
            }
        }
    }

}