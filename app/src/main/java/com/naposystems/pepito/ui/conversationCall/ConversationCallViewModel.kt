package com.naposystems.pepito.ui.conversationCall

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.pepito.dto.conversation.message.MessageReqDTO
import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.utility.Constants
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class ConversationCallViewModel @Inject constructor(
    private val repository: IContractConversationCall.Repository
) : ViewModel(), IContractConversationCall.ViewModel {

    private val _contact = MutableLiveData<Contact>()
    val contact: LiveData<Contact>
        get() = _contact

    private val _userDisplayFormat = MutableLiveData<Int>()
    val userDisplayFormat: LiveData<Int>
        get() = _userDisplayFormat

    init {
        _userDisplayFormat.value = repository.getUserDisplayFormat()
    }

    //region Implementation IContractConversationCall.ViewModel

    override fun getContact(contactId: Int) {
        viewModelScope.launch {
            _contact.value = repository.getContactById(contactId)
        }
    }

    override fun resetIsOnCallPref() {
        repository.resetIsOnCallPref()
    }

    override fun sendMissedCall(contactId: Int, isVideoCall: Boolean) {
        viewModelScope.launch {
            try {

                val messageReqDTO = MessageReqDTO(
                    userDestination = contactId,
                    quoted = "",
                    body = "",
                    numberAttachments = 0,
                    destroy = Constants.SelfDestructTime.EVERY_ONE_DAY.time,
                    messageType = if (isVideoCall) Constants.MessageType.MISSED_VIDEO_CALL.type else
                        Constants.MessageType.MISSED_CALL.type
                )

                val messageResponse = repository.sendMissedCall(messageReqDTO)

                if (messageResponse.isSuccessful) {
                    // Intentionally empty
                } else {
                    Timber.e(messageResponse.errorBody()?.toString())
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    //endregion
}
