package com.naposystems.pepito.utility.sharedViewModels.contactRepository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class ContactRepositoryShareViewModel @Inject constructor(
    private val repository: IContractContactRepositoryShare.Repository
) : ViewModel(), IContractContactRepositoryShare.ViewModel {

    private val _contactsWasLoaded = MutableLiveData<Boolean>()
    val contactsWasLoaded: LiveData<Boolean>
        get() = _contactsWasLoaded

    override fun getContacts() {
        viewModelScope.launch {
            _contactsWasLoaded.value = repository.getContacts()
        }
    }

}