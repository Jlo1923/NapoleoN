package com.naposystems.pepito.ui.blockedContacts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.pepito.entity.BlockedContact
import com.naposystems.pepito.repository.blockedContact.BlockedContactRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class BlockedContactsViewModel @Inject constructor(private val repository: BlockedContactRepository) :
    ViewModel(), IContractBlockedContact.ViewModel {

    private val _blockedContacts = MutableLiveData<List<BlockedContact>>()
    val blockedContacts: LiveData<List<BlockedContact>>
        get() = _blockedContacts

    init {
        getBlockedContacts()
    }


    //region Implementation IContractBlockedContact.ViewModel
    override fun getBlockedContacts() {
        viewModelScope.launch {
            _blockedContacts.value = repository.getBlockedContacts()
        }
    }
    //endregion
}
