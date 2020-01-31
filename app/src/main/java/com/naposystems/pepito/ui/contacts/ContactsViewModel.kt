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

    //region Implementation IContractContacts.ViewModel

    override fun getContacts() {
        viewModelScope.launch {
            try {
                _contacts = repository.getLocalContacts()
                repository.getRemoteContacts()
            } catch (ex: Exception) {
                Timber.e(ex)
            }
        }
    }

    //endregion
}
