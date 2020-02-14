package com.naposystems.pepito.ui.contacts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.pepito.entity.Contact
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class ContactsViewModel @Inject constructor(private val repository: IContractContacts.Repository) :
    ViewModel(), IContractContacts.ViewModel {

    private lateinit var _contacts: LiveData<List<Contact>>
    val contacts: LiveData<List<Contact>>
        get() = _contacts

    private val _webServiceErrors = MutableLiveData<List<String>>()
    val webServiceErrors: LiveData<List<String>>
        get() = _webServiceErrors

    private val _contactsLoaded = MutableLiveData<Boolean>()
    val contactsLoaded: LiveData<Boolean>
        get() = _contactsLoaded

    init {
        _contactsLoaded.value = false
    }

    //region Implementation IContractContacts.ViewModel

    override fun getContacts() {
        viewModelScope.launch {
            try {
                _contacts = repository.getLocalContacts()
                repository.getRemoteContacts()
                _contactsLoaded.value = true
            } catch (ex: Exception) {
                Timber.e(ex)
            }
        }
    }

    override fun sendBlockedContact(contact: Contact) {
        viewModelScope.launch {
            try {
                val response = repository.sendBlockedContact(contact)

                if (response.isSuccessful) {
                    contact.statusBlocked = true
                    repository.blockContactLocal(contact.id)
                } else {
                    _webServiceErrors.value = repository.getDefaultBlockedError(response)
                }
            } catch (ex: Exception) {
                Timber.e(ex)
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

    override fun resetContactsLoaded() {
        _contactsLoaded.value = false
    }

    //endregion
}
