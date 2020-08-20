package com.naposystems.napoleonchat.utility.sharedViewModels.contactRepository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class ContactRepositoryShareViewModel @Inject constructor(
    private val repository: IContractContactRepositoryShare.Repository
) : ViewModel(), IContractContactRepositoryShare.ViewModel {

    private val _contactsWasLoaded = MutableLiveData<Boolean>()
    val contactsWasLoaded: LiveData<Boolean>
        get() = _contactsWasLoaded

    override fun getContacts(state : String, location : Int) {
        viewModelScope.launch {
            try {
                _contactsWasLoaded.value = repository.getContacts(state)
            } catch (ex : Exception) {
                Timber.e(ex)
            }
        }
    }

}