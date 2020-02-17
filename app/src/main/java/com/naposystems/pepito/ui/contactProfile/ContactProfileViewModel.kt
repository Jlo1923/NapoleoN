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
    private val repository: IContactProfile.Repository
) : ViewModel(), IContactProfile.ViewModel {

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

    override fun getLocalContact(idContact: Int) {
        contact = repository.getLocalContact(idContact)
    }

    override fun updateNameFakeLocalContact(idContact: Int, nameFake: String) {
        viewModelScope.launch {
            repository.updateNameFakeLocalContact(idContact, nameFake)
            _responseEditNameFake.value = true
        }
    }

    override fun updateNicknameFakeLocalContact(idContact: Int, nicknameFake: String) {
        viewModelScope.launch {
            repository.updateNicknameFakeLocalContact(idContact, nicknameFake)
            _responseEditNicknameFake.value = true
        }
    }

    override fun updateAvatarFakeLocalContact(idContact: Int, avatarFake: String) {
        viewModelScope.launch {
            repository.updateAvatarFakeLocalContact(idContact, avatarFake)
        }
    }

    override fun restoreLocalContact(idContact: Int) {
        viewModelScope.launch {
            repository.restoreLocalContact(idContact)
        }
    }

    override fun deleteConversation(idContact: Int) {
        viewModelScope.launch {
            repository.deleteConversation(idContact)
        }
    }

    override fun updateContactSilenced(idContact: Int, contactSilenced: Boolean) {
        viewModelScope.launch {
            try {
                val response = repository.saveTimeMuteConversation(idContact, MuteConversationReqDTO())

                if(response.isSuccessful) {
                    repository.updateContactSilenced(idContact, Utils.convertBooleanToInvertedInt(contactSilenced))
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
}
