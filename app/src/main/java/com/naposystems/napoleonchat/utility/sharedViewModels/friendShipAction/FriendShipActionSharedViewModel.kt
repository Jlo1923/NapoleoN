package com.naposystems.napoleonchat.utility.sharedViewModels.friendShipAction

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.model.FriendShipRequest
import com.naposystems.napoleonchat.source.local.entity.MessageEntity
import com.naposystems.napoleonchat.source.remote.dto.conversation.message.MessageReqDTO
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Utils
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class FriendShipActionSharedViewModel
@Inject constructor(
    private val repository: FriendShipActionSharedRepository,
    private val context: Context
) : ViewModel() {

    private val _friendshipRequestAcceptedSuccessfully = MutableLiveData<Boolean>()
    val friendshipRequestAcceptedSuccessfully: LiveData<Boolean>
        get() = _friendshipRequestAcceptedSuccessfully

    private val _friendshipRequestPutSuccessfully = MutableLiveData<Boolean>()
    val friendshipRequestPutSuccessfully: LiveData<Boolean>
        get() = _friendshipRequestPutSuccessfully

    private val _friendshipRequestWsError = MutableLiveData<String>()
    val friendshipRequestWsError: LiveData<String>
        get() = _friendshipRequestWsError

    fun refuseFriendshipRequest(friendShipRequest: FriendShipRequest) {
        viewModelScope.launch {
            try {
                val response = repository.refuseFriendshipRequest(friendShipRequest)

                if (response.isSuccessful) {
                    _friendshipRequestPutSuccessfully.value = true
                } else {
                    _friendshipRequestWsError.value = repository.getError(response)
                }
            } catch (ex: Exception) {
                _friendshipRequestWsError.value = context.getString(R.string.text_fail)
            }
        }
    }

    fun acceptFriendshipRequest(friendShipRequest: FriendShipRequest) {
        viewModelScope.launch {
            try {
                val response = repository.acceptFriendshipRequest(friendShipRequest)

                val uuidToSend = UUID.randomUUID().toString()

                if (response.isSuccessful) {
                    repository.addContact(friendShipRequest)
                    _friendshipRequestAcceptedSuccessfully.value = true

                    val body = "${context.getString(R.string.text_new_contact)}\u00A0 "

                    val messageReqDTO = MessageReqDTO(
                        userDestination = friendShipRequest.contact.id,
                        quoted = "",
                        body = body,
                        numberAttachments = 0,
                        destroy = Constants.SelfDestructTime.EVERY_SEVEN_DAY.time,
                        messageType = Constants.MessageTextType.NEW_CONTACT.type,
                        uuidSender = uuidToSend
                    )

                    val responseMessage = repository.sendNewContactMessage(messageReqDTO)

                    if (responseMessage.isSuccessful) {

                        val currentTime =
                            TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()).toInt()

                        val message = MessageEntity(
                            id = 0,
                            webId = "",
                            uuid = uuidToSend,
                            body = body,
                            quoted = "",
                            contactId = friendShipRequest.contact.id,
                            updatedAt = 0,
                            createdAt = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
                                .toInt(),
                            isMine = Constants.IsMine.YES.value,
                            status = Constants.MessageStatus.SENT.status,
                            numberAttachments = 0,
                            messageType = Constants.MessageTextType.NEW_CONTACT.type,
                            selfDestructionAt = Constants.SelfDestructTime.EVERY_SEVEN_DAY.time,
                            totalSelfDestructionAt = currentTime.plus(
                                Utils.convertItemOfTimeInSeconds(Constants.SelfDestructTime.EVERY_SEVEN_DAY.time)
                            )
                        )

                        repository.insertMessage(message).toInt()
                    }

                } else {
                    _friendshipRequestWsError.value = repository.getError(response)
                }
            } catch (ex: Exception) {
                Timber.e(ex)
                _friendshipRequestWsError.value = context.getString(R.string.text_fail)
            }
        }
    }

    fun cancelFriendshipRequest(friendShipRequest: FriendShipRequest) {
        viewModelScope.launch {
            try {
                val response = repository.cancelFriendshipRequest(friendShipRequest)

                if (response.isSuccessful) {
                    _friendshipRequestPutSuccessfully.value = true
                } else {
                    _friendshipRequestWsError.value = repository.getError(response)
                }
            } catch (ex: Exception) {
                _friendshipRequestWsError.value = context.getString(R.string.text_fail)
            }
        }
    }

    fun clearMessageError() {
        _friendshipRequestWsError.value = null
    }
}