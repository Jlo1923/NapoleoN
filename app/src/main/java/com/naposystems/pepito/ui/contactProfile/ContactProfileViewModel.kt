package com.naposystems.pepito.ui.contactProfile

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.pepito.R
import com.naposystems.pepito.dto.muteConversation.MuteConversationReqDTO
import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.utility.Utils
import kotlinx.coroutines.launch
import javax.inject.Inject

class ContactProfileViewModel @Inject constructor(
    private val context: Context,
    private val repository: IContractContactProfile.Repository
) : ViewModel(), IContractContactProfile.ViewModel {

    lateinit var contact: LiveData<Contact>

    private val _responseEditNameFake = MutableLiveData<Boolean>()
    val responseEditNameFake: LiveData<Boolean>
        get() = _responseEditNameFake

    private val _responseEditNicknameFake = MutableLiveData<Boolean>()
    val responseEditNicknameFake: LiveData<Boolean>
        get() = _responseEditNicknameFake

    private val _muteConversationWsError = MutableLiveData<List<String>>()
    val muteConversationWsError: LiveData<List<String>>
        get() = _muteConversationWsError

    override fun getLocalContact(contactId: Int) {
        contact = repository.getLocalContact(contactId)
    }

    override fun updateNameFakeContact(contactId: Int, nameFake: String) {
        viewModelScope.launch {
            repository.updateNameFakeContact(contactId, nameFake)
            _responseEditNameFake.value = true
        }
    }

    override fun updateNicknameFakeContact(contactId: Int, nicknameFake: String) {
        viewModelScope.launch {
            repository.updateNicknameFakeContact(contactId, nicknameFake)
            _responseEditNicknameFake.value = true
        }
    }

    override fun updateAvatarFakeContact(contactId: Int, avatarFake: String) {
        viewModelScope.launch {
            repository.updateAvatarFakeContact(contactId, avatarFake)
        }
    }

    override fun restoreContact(contactId: Int) {
        viewModelScope.launch {
            repository.restoreContact(contactId)
        }
    }

    override fun updateContactSilenced(contactId: Int, contactSilenced: Boolean) {
        viewModelScope.launch {
            try {
                val response = repository.saveTimeMuteConversation(contactId, MuteConversationReqDTO())

                if(response.isSuccessful) {
                    repository.updateContactSilenced(contactId, Utils.convertBooleanToInvertedInt(contactSilenced))
                } else {
                    _muteConversationWsError.value = repository.getError(response)
                }
            } catch (ex: Exception) {
                val errorList = ArrayList<String>()
                errorList.add(context.getString(R.string.text_fail))
                _muteConversationWsError.value = errorList
            }
        }
    }

    override fun restoreImageByContact(contactId: Int) {
        viewModelScope.launch {
            repository.restoreImageByContact(contactId)
        }
    }
}
