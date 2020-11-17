package com.naposystems.napoleonchat.ui.addContact

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.dto.addContact.FriendshipRequestsResDTO
import com.naposystems.napoleonchat.dto.contacts.ContactResDTO
import com.naposystems.napoleonchat.entity.Contact
import com.naposystems.napoleonchat.entity.addContact.FriendShipRequestAdapterType
import kotlinx.coroutines.launch
import javax.inject.Inject

class AddContactViewModel @Inject constructor(
    private val repository: IContractAddContact.Repository,
    private val context: Context
) :
    ViewModel(), IContractAddContact.ViewModel {

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

    private val _friendshipRequestWsError = MutableLiveData<String>()
    val friendshipRequestWsError: LiveData<String>
        get() = _friendshipRequestWsError

    init {
        _users.value = emptyList()
        _friendShipRequestSendSuccessfully.value = false
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
                val filterQuery = query.replace("@", "")
                val response = repository.searchContact(filterQuery)

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
    //endregion
}
