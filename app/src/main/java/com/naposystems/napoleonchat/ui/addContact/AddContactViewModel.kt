package com.naposystems.napoleonchat.ui.addContact

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.model.FriendShipRequest
import com.naposystems.napoleonchat.model.FriendShipRequestAdapterType
import com.naposystems.napoleonchat.model.addContact.Contact
import com.naposystems.napoleonchat.repository.addContact.AddContactRepository
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.source.remote.dto.addContact.FriendshipRequestsResDTO
import com.naposystems.napoleonchat.source.remote.dto.contacts.ContactResDTO
import kotlinx.coroutines.launch
import javax.inject.Inject

class AddContactViewModel @Inject constructor(
    private val repository: AddContactRepository,
    private val context: Context
) : ViewModel() {

    private val _users = MutableLiveData<MutableList<Contact>>()
    val users: LiveData<MutableList<Contact>>
        get() = _users

    private val _opened = MutableLiveData<Boolean>()
    val opened: LiveData<Boolean>
        get() = _opened

    private val _friendShipRequestSendSuccessfully = MutableLiveData<Contact>()
    val friendShipRequestSendSuccessfully: LiveData<Contact>
        get() = _friendShipRequestSendSuccessfully

    private val _friendshipRequests = MutableLiveData<List<FriendShipRequestAdapterType>>()
    val friendshipRequests: LiveData<List<FriendShipRequestAdapterType>>
        get() = _friendshipRequests

    private val _friendshipRequestWsError = MutableLiveData<String>()
    val friendshipRequestWsError: LiveData<String>
        get() = _friendshipRequestWsError

    private val _updateItem = MutableLiveData<Contact>()
    val updateItem: LiveData<Contact> get() = _updateItem

    private lateinit var contactModel: Contact
    private var isOffer: Boolean = false

    init {
        _users.value = mutableListOf()

    }

    //region Implementation IContractAddContact.ViewModel
    fun getFriendshipRequests() {

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


    fun searchContact(query: String) {

        viewModelScope.launch {
            try {
                val filterQuery = query.replace("@", "")
                val response = repository.searchContact(filterQuery)

                if (response.isSuccessful) {
                    val list = response.body()?.let { ContactResDTO.getUsers(it) }
                    _users.value = list

                } else {
                    _friendshipRequestWsError.value = context.getString(R.string.text_fail)
                }
            } catch (ex: Exception) {
                _friendshipRequestWsError.value = context.getString(R.string.text_fail)
            }
        }
    }

    fun resetContacts() {
        _users.value = mutableListOf()
    }

    fun getUsers(): List<Any>? {
        return _users.value
    }

    fun getSearchOpened(): Boolean? {
        return _opened.value ?: false
    }

    fun setSearchOpened() {
        _opened.value = true
    }

    fun getRequestSend(): List<FriendShipRequestAdapterType>? {
        return _friendshipRequests.value
    }

    fun sendFriendshipRequest(contact: Contact) {
        viewModelScope.launch {
            try {
                val response = repository.sendFriendshipRequest(contact)
                if (response.isSuccessful) {
                    _friendShipRequestSendSuccessfully.value = contact
                }
            } catch (ex: Exception) {
                _friendshipRequestWsError.value = context.getString(R.string.text_fail)
            }
        }
    }

    fun acceptOrRefuseRequest(contact: Contact, state: Boolean): FriendShipRequest {
        contactModel = contact
        isOffer = true
        return FriendShipRequest(
            contact.offerId ?: 0,
            0,
            0,
            "",
            "",
            ContactResDTO.toEntity(contact),
            true
        )
    }

    fun validateIfExistsOffer() {
        if (isOffer) {
            _updateItem.value = contactModel
            isOffer = false
        }
    }

    fun getContact(contact: Contact): ContactEntity? {
        return repository.getContact(contact.id)
    }
    //endregion
}
