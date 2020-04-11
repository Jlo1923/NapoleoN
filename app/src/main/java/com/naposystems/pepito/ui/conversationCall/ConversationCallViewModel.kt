package com.naposystems.pepito.ui.conversationCall

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.pepito.entity.Contact
import kotlinx.coroutines.launch
import javax.inject.Inject

class ConversationCallViewModel @Inject constructor(
    private val repository: IContractConversationCall.Repository
) : ViewModel(), IContractConversationCall.ViewModel {

    private val _contact = MutableLiveData<Contact>()
    val contact: LiveData<Contact>
        get() = _contact

    //region Implementation IContractConversationCall.ViewModel

    override fun getContact(contactId: Int) {
        viewModelScope.launch {
            _contact.value = repository.getContactById(contactId)
        }
    }

    override fun resetIsOnCallPref() {
        repository.resetIsOnCallPref()
    }

    //endregion
}
