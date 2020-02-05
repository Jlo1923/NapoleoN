package com.naposystems.pepito.ui.contactProfile

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.pepito.entity.Contact
import kotlinx.coroutines.launch
import javax.inject.Inject

class ContactProfileViewModel @Inject constructor(
    private val context: Context,
    private val repository: IContactProfile.Repository
) : ViewModel(), IContactProfile.ViewModel {

    lateinit var contact: LiveData<Contact>

    override fun getLocalContact(idContact: Int) {
        contact = repository.getLocalContact(idContact)
    }

}
