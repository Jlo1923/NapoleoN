package com.naposystems.napoleonchat.ui.conversationCall

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.napoleonchat.model.CallModel
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.source.remote.dto.cancelCall.CancelCallReqDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.message.MessageReqDTO
import com.naposystems.napoleonchat.utility.Constants
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class ConversationCallViewModel
@Inject constructor(
    private val repository: IContractConversationCall.Repository
) : ViewModel(), IContractConversationCall.ViewModel {

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

    override fun getContact(contactId: Int) {
        viewModelScope.launch {
            _contact.value = repository.getContactById(contactId)
        }
    }

    override fun sendMissedCall(callModel: CallModel) {
        viewModelScope.launch {
            try {

                val messageReqDTO = MessageReqDTO(
                    userDestination = callModel.contactId,
                    quoted = "",
                    body = "",
                    numberAttachments = 0,
                    destroy = Constants.SelfDestructTime.EVERY_ONE_DAY.time,
                    messageType = if (callModel.isVideoCall) Constants.MessageTextType.MISSED_VIDEO_CALL.type
                    else Constants.MessageTextType.MISSED_CALL.type
                )

                val messageResponse = repository.sendMissedCall(messageReqDTO)

                if (!messageResponse.isSuccessful) {
                    Timber.e(messageResponse.errorBody()?.toString())
                }

            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    override fun cancelCall(callModel: CallModel) {
        GlobalScope.launch {
            try {
                val cancelCallReqDTO = CancelCallReqDTO(
                    callModel.contactId,
                    callModel.channelName
                )
                val response = repository.cancelCall(cancelCallReqDTO)

                if (!response.isSuccessful) {
                    Timber.e(response.errorBody()?.toString())
                }

            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

//endregion
}
