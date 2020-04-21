package com.naposystems.pepito.utility.sharedViewModels.contactProfile

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.naposystems.pepito.entity.Contact
import javax.inject.Inject

class ContactProfileShareViewModel @Inject constructor(
    private val repository: IContractContactProfileShare.Repository
) : ViewModel(), IContractContactProfileShare.ViewModel {

    private lateinit var _contact : LiveData<Contact>
    val contact : LiveData<Contact>
    get() = _contact

    override fun getLocalContact(contactId: Int) {
        _contact = repository.getLocalContact(contactId)
    }
}