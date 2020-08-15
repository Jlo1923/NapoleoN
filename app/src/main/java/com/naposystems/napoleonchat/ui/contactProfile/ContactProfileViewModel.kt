package com.naposystems.napoleonchat.ui.contactProfile

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.dto.muteConversation.MuteConversationReqDTO
import com.naposystems.napoleonchat.utility.Utils
import kotlinx.coroutines.launch
import javax.inject.Inject

class ContactProfileViewModel @Inject constructor(
    private val context: Context,
    private val repository: IContractContactProfile.Repository
) : ViewModel(), IContractContactProfile.ViewModel {

    private val _muteConversationWsError = MutableLiveData<List<String>>()
    val muteConversationWsError: LiveData<List<String>>
        get() = _muteConversationWsError

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
