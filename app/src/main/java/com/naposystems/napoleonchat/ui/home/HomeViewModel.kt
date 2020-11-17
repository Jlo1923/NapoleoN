package com.naposystems.napoleonchat.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.napoleonchat.dto.addContact.FriendshipRequestReceivedDTO
import com.naposystems.napoleonchat.entity.Contact
import com.naposystems.napoleonchat.entity.User
import com.naposystems.napoleonchat.entity.addContact.FriendShipRequest
import com.naposystems.napoleonchat.entity.message.MessageAndAttachment
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class HomeViewModel @Inject constructor(private val repository: IContractHome.Repository) :
    ViewModel(), IContractHome.ViewModel {

    private lateinit var _user: LiveData<User>
    val user: LiveData<User>
        get() = _user

    private var _conversations: LiveData<List<MessageAndAttachment>>?
    val conversations: LiveData<List<MessageAndAttachment>>?
        get() = _conversations

    private val _quantityFriendshipRequest = MutableLiveData<Int>()
    val quantityFriendshipRequest: LiveData<Int>
        get() = _quantityFriendshipRequest

    private val _friendShipRequestReceived = MutableLiveData<List<FriendShipRequest>>()
    val friendShipRequestReceived: LiveData<List<FriendShipRequest>>
        get() = _friendShipRequestReceived

    private val _jsonCleaned = MutableLiveData<String>()
    val jsonCleaned: LiveData<String>
        get() = _jsonCleaned

    private val _jsonNotification = MutableLiveData<String>()
    val jsonNotification: LiveData<String>
        get() = _jsonNotification

    private val _contact = MutableLiveData<Contact>()
    val contact: LiveData<Contact>
        get() = _contact

    init {
        _conversations = null
        _contact.value = null
        _jsonNotification.value = null
        _jsonCleaned.value = null
        _quantityFriendshipRequest.value = -1
    }

    //region Implementation IContractHome.ViewModel
    override fun getFriendshipQuantity() {
        viewModelScope.launch {
            try {
                val response = repository.getFriendshipQuantity()

                if (response.isSuccessful) {
                    val friendshipRequestReceived =
                        response.body()!!.quantityFriendshipRequestReceived

                    _quantityFriendshipRequest.value = friendshipRequestReceived
                }
            } catch (ex: Exception) {
                Timber.e(ex)
            }
        }
    }

    override fun getFriendshipRequestHome() {
        viewModelScope.launch {
            try {
                val response = repository.getFriendshipRequestHome()

                if (response.isSuccessful) {
                    response.body()?.let {
                        _friendShipRequestReceived.value =
                            FriendshipRequestReceivedDTO.toListFriendshipRequestReceivedEntity(it)
                        getFriendshipQuantity()
                    }
                }
            } catch (ex: Exception) {
                Timber.e(ex)
            }
        }
    }

    override fun getConversation() {
        viewModelScope.launch {
            _conversations = repository.getMessagesForHome()
        }
    }

    override fun getUserLiveData() {
        viewModelScope.launch {
            _user = repository.getUserLiveData()
        }
    }

    override fun subscribeToGeneralSocketChannel() {
        viewModelScope.launch {
            repository.subscribeToGeneralSocketChannel()
        }
    }

    override fun getMessages() {
        viewModelScope.launch {
            try {
                repository.getRemoteMessages()
            } catch (ex: Exception) {
                Timber.e(ex)
            }
        }
    }

    override fun getDeletedMessages() {
        viewModelScope.launch {
            try {
                repository.getDeletedMessages()
            } catch (ex: Exception) {
                Timber.e(ex)
            }
        }
    }

    override fun insertSubscription() {
        viewModelScope.launch {
            repository.insertSubscription()
        }
    }

    override fun getFreeTrial(): Long {
        return repository.getFreeTrial()
    }

    override fun getSubscriptionTime(): Long {
        return repository.getSubscriptionTime()
    }

    override fun getJsonNotification() {
        _jsonNotification.value = repository.getJsonNotification()
    }

    override fun getContact(contactId: Int) {
        viewModelScope.launch {
            _contact.value = repository.getContact(contactId)
        }
    }

    override fun cleanJsonNotification(json: String) {
        viewModelScope.launch {
            repository.cleanJsonNotification()
            _jsonCleaned.value = json
        }
    }

    override fun resetConversations() {
        _conversations = null
    }

    override fun cleanVariables() {
        _contact.value = null
        _jsonNotification.value = null
        _jsonCleaned.value = null
    }

    override fun verifyMessagesToDelete() {
        repository.verifyMessagesToDelete()
    }

    //endregion
}
