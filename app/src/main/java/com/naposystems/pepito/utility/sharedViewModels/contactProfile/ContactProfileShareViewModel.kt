package com.naposystems.pepito.utility.sharedViewModels.contactProfile

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.naposystems.pepito.entity.Contact
import javax.inject.Inject

class ContactProfileShareViewModel @Inject constructor(
    private val repository: IContractContactProfileShare.Repository
) : ViewModel(), IContractContactProfileShare.ViewModel {

    lateinit var contact: LiveData<Contact>

    override fun getLocalContact(contactId: Int) {
        contact = repository.getLocalContact(contactId)
    }
}