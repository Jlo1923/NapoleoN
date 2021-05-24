package com.naposystems.napoleonchat.ui.contactProfile

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.repository.contactProfile.ContactProfileRepository
import com.naposystems.napoleonchat.source.remote.dto.muteConversation.MuteConversationReqDTO
import com.naposystems.napoleonchat.utility.Utils
import kotlinx.coroutines.launch
import javax.inject.Inject

class ContactProfileViewModel @Inject constructor(
    private val context: Context,
    private val repository: ContactProfileRepository
) : ViewModel() {

    private val _contactProfileWsError = MutableLiveData<String>()
    val contactProfileWsError: LiveData<String> get() = _contactProfileWsError

    private val _muteConversationWsError = MutableLiveData<List<String>>()
    val muteConversationWsError: LiveData<List<String>>
        get() = _muteConversationWsError

    fun updateAvatarFakeContact(contactId: Int, avatarFake: String) {
        viewModelScope.launch {
            try {
                val response = repository.updateAvatarFakeContact(contactId, avatarFake)
                if (response.isSuccessful) {
                    response.body()?.let { repository.updateContactFakeLocal(contactId, it, false) }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                _contactProfileWsError.value = context.getString(R.string.text_fail)
            }
        }
    }

    fun restoreContact(contactId: Int) {
        viewModelScope.launch {
            try {
                val response = repository.restoreContact(contactId)
                if (response.isSuccessful) {
                    response.body()?.let { repository.updateContactFakeLocal(contactId, it, true) }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                _contactProfileWsError.value = context.getString(R.string.text_fail)
            }
        }
    }

    fun updateContactSilenced(contactId: Int, contactSilenced: Boolean) {
        viewModelScope.launch {
            try {
                val response =
                    repository.saveTimeMuteConversation(contactId, MuteConversationReqDTO())

                if (response.isSuccessful) {
                    repository.updateContactSilenced(
                        contactId,
                        Utils.convertBooleanToInvertedInt(contactSilenced)
                    )
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

    fun restoreImageByContact(contactId: Int) {
        viewModelScope.launch {
            repository.restoreImageByContact(contactId)
        }
    }
}
