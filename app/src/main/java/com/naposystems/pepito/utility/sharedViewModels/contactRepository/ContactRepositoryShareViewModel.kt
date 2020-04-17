package com.naposystems.pepito.utility.sharedViewModels.contactRepository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class ContactRepositoryShareViewModel @Inject constructor(
    private val repository: IContractContactRepositoryShare.Repository
) : ViewModel(), IContractContactRepositoryShare.ViewModel {

    override fun getContacts() {
        viewModelScope.launch {
            repository.getContacts()
        }
    }

}