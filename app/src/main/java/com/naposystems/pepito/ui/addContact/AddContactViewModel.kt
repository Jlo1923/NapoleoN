package com.naposystems.pepito.ui.addContact

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.pepito.BuildConfig
import com.naposystems.pepito.R
import com.naposystems.pepito.crypto.message.CryptoMessage
import com.naposystems.pepito.dto.addContact.FriendshipRequestsResDTO
import com.naposystems.pepito.dto.contacts.ContactResDTO
import com.naposystems.pepito.dto.conversation.message.MessageReqDTO
import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.entity.addContact.FriendShipRequest
import com.naposystems.pepito.entity.addContact.FriendShipRequestAdapterType
import com.naposystems.pepito.entity.message.Message
import com.naposystems.pepito.utility.Constants
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AddContactViewModel @Inject constructor(
    private val repository: IContractAddContact.Repository,
    private val context: Context
) :
    ViewModel(), IContractAddContact.ViewModel {

    private val cryptoMessage = CryptoMessage(context)

    lateinit var lastFriendshipRequest: Contact

    private val _users = MutableLiveData<List<Contact>>()
    val users: LiveData<List<Contact>>
        get() = _users

    private val _opened = MutableLiveData<Boolean>()
    val opened: LiveData<Boolean>
        get() = _opened

    private val _friendShipRequestSendSuccessfully = MutableLiveData<Boolean>()
    val friendShipRequestSendSuccessfully: LiveData<Boolean>
        get() = _friendShipRequestSendSuccessfully

    private val _friendshipRequests = MutableLiveData<List<FriendShipRequestAdapterType>>()
    val friendshipRequests: LiveData<List<FriendShipRequestAdapterType>>
        get() = _friendshipRequests

    private val _friendshipRequestPutSuccessfully = MutableLiveData<Boolean>()
    val friendshipRequestPutSuccessfully: LiveData<Boolean>
        get() = _friendshipRequestPutSuccessfully

    private val _friendshipRequestWsError = MutableLiveData<String>()
    val friendshipRequestWsError: LiveData<String>
        get() = _friendshipRequestWsError

    private val _friendshipRequestAcceptedSuccessfully = MutableLiveData<Boolean>()
    val friendshipRequestAcceptedSuccessfully: LiveData<Boolean>
        get() = _friendshipRequestAcceptedSuccessfully

    init {
        _users.value = emptyList()
        _friendShipRequestSendSuccessfully.value = false
        _friendshipRequestWsError.value = ""
        _friendshipRequestPutSuccessfully.value = false
        _friendshipRequestAcceptedSuccessfully.value = false
    }

    //region Implementation IContractAddContact.ViewModel
    override fun getFriendshipRequests() {
        viewModelScope.launch {
            try {
                val response = repository.getFriendshipRequest()

                if (response.isSuccessful) {
                    _friendshipRequests.value = FriendshipRequestsResDTO
                        .toListFriendshipRequestEntity(response.body()!!, context)
                }
            } catch (ex: Exception) {
                _friendshipRequestWsError.value = context.getString(R.string.text_fail)
            }
        }
    }

    override fun searchContact(query: String) {
        viewModelScope.launch {
            try {
                val response = repository.searchContact(query)

                if (response.isSuccessful) {
                    _users.value = ContactResDTO.toEntityList(response.body()!!)
                } else {
                    _friendshipRequestWsError.value = context.getString(R.string.text_fail)
                }
            } catch (ex: Exception) {
                _friendshipRequestWsError.value = context.getString(R.string.text_fail)
            }
        }
    }

    override fun resetContacts() {
        _users.value = emptyList()
    }

    override fun getUsers(): List<Contact>? {
        return _users.value
    }

    override fun getSearchOpened(): Boolean? {
        return _opened.value ?: false
    }

    override fun setSearchOpened() {
        _opened.value = true
    }

    override fun getRequestSend(): List<FriendShipRequestAdapterType>? {
        return _friendshipRequests.value
    }

    override fun sendFriendshipRequest(contact: Contact) {
        viewModelScope.launch {
            try {
                lastFriendshipRequest = contact
                val response = repository.sendFriendshipRequest(contact)

                if (response.isSuccessful) {
                    _friendShipRequestSendSuccessfully.value = true
                }
            } catch (ex: Exception) {
                _friendshipRequestWsError.value = context.getString(R.string.text_fail)
            }
        }
    }

    override fun cancelFriendshipRequest(friendShipRequest: FriendShipRequest) {
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

    override fun refuseFriendshipRequest(friendShipRequest: FriendShipRequest) {
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

    override fun acceptFriendshipRequest(friendShipRequest: FriendShipRequest) {
        viewModelScope.launch {
            try {
                val response = repository.acceptFriendshipRequest(friendShipRequest)

                if (response.isSuccessful) {
                    repository.addContact(friendShipRequest)
                    _friendshipRequestAcceptedSuccessfully.value = true

                    val body = "@yo soy tu nuevo contacto 😎"

                    val messageReqDTO = MessageReqDTO(
                        userDestination = friendShipRequest.contact.id,
                        quoted = "",
                        body = body,
                        numberAttachments = 0,
                        destroy = Constants.SelfDestructTime.EVERY_SEVEN_DAY.time,
                        messageType = Constants.MessageType.SYSTEM_MESSAGE.type
                    )

                    val responseMessage = repository.sendNewContactMessage(messageReqDTO)

                    if (responseMessage.isSuccessful) {
                        val message = Message(
                            id = 0,
                            webId = "",
                            body = body,
                            quoted = "quote",
                            contactId = friendShipRequest.contact.id,
                            updatedAt = 0,
                            createdAt = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
                                .toInt(),
                            isMine = Constants.IsMine.YES.value,
                            status = Constants.MessageStatus.SENT.status,
                            numberAttachments = 0,
                            messageType = Constants.MessageType.SYSTEM_MESSAGE.type,
                            selfDestructionAt = Constants.SelfDestructTime.EVERY_SEVEN_DAY.time
                        )

                        if (BuildConfig.ENCRYPT_API) {
                            message.encryptBody(cryptoMessage)
                        }

                        repository.insertMessage(message).toInt()
                    }

                } else {
                    _friendshipRequestWsError.value = repository.getError(response)
                }
            } catch (ex: Exception) {
                _friendshipRequestWsError.value = context.getString(R.string.text_fail)
            }
        }
    }

    //endregion
}
