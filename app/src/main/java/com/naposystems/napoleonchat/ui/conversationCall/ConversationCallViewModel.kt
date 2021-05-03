package com.naposystems.napoleonchat.ui.conversationCall

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.napoleonchat.app.NapoleonApplication
import com.naposystems.napoleonchat.repository.conversationCall.ConversationCallRepository
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.source.remote.dto.cancelCall.CancelCallReqDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.call.reject.RejectCallReqDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.message.MessageReqDTO
import com.naposystems.napoleonchat.utility.Constants
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class ConversationCallViewModel
@Inject constructor(
    private val repository: ConversationCallRepository
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
        viewModelScope.launch {
            try {
                NapoleonApplication.callModel?.let { callModel ->
                    val messageReqDTO = MessageReqDTO(
                        userDestination = callModel.contactId,
                        quoted = "",
                        body = "",
                        numberAttachments = 0,
                        destroy = Constants.SelfDestructTime.EVERY_ONE_DAY.time,
                        messageType = if (callModel.isVideoCall) Constants.MessageTextType.MISSED_VIDEO_CALL.type else Constants.MessageTextType.MISSED_CALL.type,
                        uuidSender = UUID.randomUUID().toString()
                    )
                    repository.sendMissedCall(messageReqDTO)
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    fun cancelCall() {
        GlobalScope.launch {
            try {
                NapoleonApplication.callModel?.let { callModel ->
                    val cancelCallReqDTO = CancelCallReqDTO(
                        callModel.contactId,
                        callModel.channelName
                    )
                    repository.cancelCall(cancelCallReqDTO)
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    fun rejectCall() {
        GlobalScope.launch {
            try {
                NapoleonApplication.callModel?.let { callModel ->
                    val rejectCallReqDTO = RejectCallReqDTO(
                        callModel.contactId,
                        callModel.channelName
                    )
                    repository.rejectCall(rejectCallReqDTO)
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

//endregion
}
