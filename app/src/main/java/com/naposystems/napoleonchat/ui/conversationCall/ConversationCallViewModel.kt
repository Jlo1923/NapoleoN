package com.naposystems.napoleonchat.ui.conversationCall

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.napoleonchat.repository.conversationCall.ConversationCallRepository
import com.naposystems.napoleonchat.service.syncManager.SyncManager
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import kotlinx.coroutines.launch
import javax.inject.Inject

class ConversationCallViewModel
@Inject constructor(
    private val repository: ConversationCallRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _contact = MutableLiveData<ContactEntity>()
    val contact: LiveData<ContactEntity>
        get() = _contact

    private val _userDisplayFormat = MutableLiveData<Int>()
    val userDisplayFormat: LiveData<Int>
        get() = _userDisplayFormat

    init {
        _userDisplayFormat.value = repository.getUserDisplayFormat()
    }

    //region Implementation IContractConversationCall.ViewModel

    fun getContact(contactId: Int) {
        viewModelScope.launch {
            _contact.value = repository.getContactById(contactId)
        }
    }

    fun sendMissedCall() {
        syncManager.sendMissedCall()
    }

    fun cancelCall() {
        syncManager.cancelCall()
    }

    fun rejectCall() {
        syncManager.rejectCall()
    }

//endregion
}
