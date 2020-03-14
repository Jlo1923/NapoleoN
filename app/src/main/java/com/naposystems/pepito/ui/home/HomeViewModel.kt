package com.naposystems.pepito.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.pepito.entity.User
import com.naposystems.pepito.entity.conversation.ConversationAndContact
import com.naposystems.pepito.model.typeSubscription.SubscriptionUser
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class HomeViewModel @Inject constructor(private val repository: IContractHome.Repository) :
    ViewModel(), IContractHome.ViewModel {

    private val _user = MutableLiveData<User>()
    val user: LiveData<User>
        get() = _user

    private val _subscriptionUser = MutableLiveData<SubscriptionUser>()
    val subscriptionUser: LiveData<SubscriptionUser>
        get() = _subscriptionUser

    private val _quantityFriendshipRequest = MutableLiveData<Int>()
    val quantityFriendshipRequest: LiveData<Int>
        get() = _quantityFriendshipRequest

    val conversations: LiveData<List<ConversationAndContact>> = repository.getConversations()

    init {
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

            }
        }
    }

    override fun getUser(): User {
        viewModelScope.launch {
            _user.value = repository.getUser()
        }
        return _user.value!!
    }

    override fun subscribeToGeneralSocketChannel() {
        viewModelScope.launch {
            repository.subscribeToGeneralSocketChannel()
        }
    }

    override fun getContactsAndMessages() {
        viewModelScope.launch {
            try {
                repository.getContacts()
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
    //endregion
}
