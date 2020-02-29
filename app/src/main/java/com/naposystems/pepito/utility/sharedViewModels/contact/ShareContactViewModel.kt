package com.naposystems.pepito.utility.sharedViewModels.contact

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.repository.sharedRepository.ShareContactRepository
import com.naposystems.pepito.utility.sharedViewModels.contact.IContractShareContact
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class ShareContactViewModel @Inject constructor(
    private val repository: ShareContactRepository
): ViewModel(), IContractShareContact.ViewModel  {

    private val _webServiceErrors = MutableLiveData<List<String>>()
    val webServiceErrors: LiveData<List<String>>
        get() = _webServiceErrors

    override fun sendBlockedContact(contact: Contact) {
        viewModelScope.launch {
            try {
                val response = repository.sendBlockedContact(contact)

                if (response.isSuccessful) {
                    contact.statusBlocked = true
                    repository.blockContactLocal(contact)
                } else {
                    _webServiceErrors.value = repository.getDefaultBlockedError(response)
                }
            } catch (ex: Exception) {
                Timber.e(ex)
            }
        }
    }

    override fun unblockContact(contact: Contact) {
        viewModelScope.launch {
            try {
                val response = repository.unblockContact(contact)

                if (response.isSuccessful) {
                    contact.statusBlocked = false
                    repository.unblockContactLocal(contact.id)
                } else {
                    _webServiceErrors.value = repository.getDefaultUnblockError(response)
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    override fun sendDeleteContact(contact: Contact) {
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

    override fun deleteConversation(contactId: Int) {
        viewModelScope.launch {
            repository.deleteConversation(contactId)
        }
    }
}