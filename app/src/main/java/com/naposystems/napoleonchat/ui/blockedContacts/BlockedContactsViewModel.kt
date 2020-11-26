package com.naposystems.napoleonchat.ui.blockedContacts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.napoleonchat.entity.Contact
import com.naposystems.napoleonchat.repository.blockedContact.BlockedContactRepository
import com.naposystems.napoleonchat.utility.Utils
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class BlockedContactsViewModel @Inject constructor(private val repository: BlockedContactRepository) :
    ViewModel(), IContractBlockedContact.ViewModel {

    private lateinit var _blockedContacts: LiveData<List<Contact>>
    val blockedContacts: LiveData<List<Contact>>
        get() = _blockedContacts

    private val _listBlockedContacts = MutableLiveData<List<Contact>>()
    val listBlockedContacts: LiveData<List<Contact>>
        get() = _listBlockedContacts

    private val _webServiceErrors = MutableLiveData<List<String>>()
    val webServiceErrors: LiveData<List<String>>
        get() = _webServiceErrors

    //region Implementation IContractBlockedContact.ViewModel
    override fun getBlockedContacts() {
        viewModelScope.launch {
            try {
                _blockedContacts =  repository.getBlockedContactsLocal()
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    override fun searchLocalBlockedContact(query: String) {
        _listBlockedContacts.value = _blockedContacts.value!!.filter {
            Utils.validateNickname(it, query) || Utils.validateDisplayName(it, query)
        }
    }

    //endregion
}
