package com.naposystems.napoleonchat.utility.sharedViewModels.contactProfile

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import javax.inject.Inject

class ContactProfileShareViewModel @Inject constructor(
    private val repository: ContactProfileShareRepository
) : ViewModel() {

    private lateinit var _contact: LiveData<ContactEntity>
    val contact: LiveData<ContactEntity>
        get() = _contact

    fun getLocalContact(contactId: Int) {
        _contact = repository.getLocalContact(contactId)
    }
}