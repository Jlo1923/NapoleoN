package com.naposystems.napoleonchat.ui.addContact

import android.content.Context
import androidx.databinding.BaseObservable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.source.remote.dto.addContact.FriendshipRequestsResDTO
import com.naposystems.napoleonchat.source.remote.dto.contacts.ContactResDTO
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.model.FriendShipRequestAdapterType
import com.naposystems.napoleonchat.model.addContact.AddContactTitle
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class AddContactViewModel @Inject constructor(
    private val repository: IContractAddContact.Repository,
    private val context: Context
) :
    ViewModel(), IContractAddContact.ViewModel {

    lateinit var lastFriendshipRequest: ContactEntity

    private val _users = MutableLiveData<MutableList<Any>>()
    val users: LiveData<MutableList<Any>>
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

    private val _updateItem = MutableLiveData<ContactEntity>()
    val updateItem: LiveData<ContactEntity> get() = _updateItem

    private lateinit var contactEntity: ContactEntity
    private var isOffer: Boolean = false

    init {
        _users.value = mutableListOf()
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

                val title1 = AddContactTitle()
                title1.id =1
                title1.title = context.getString(R.string.text_my_contacts_added)

                val title2 = AddContactTitle()
                title2.id =2
                title2.title = context.getString(R.string.text_coincidence)

                if (response.isSuccessful) {
                    val multableList: MutableList<Any> = mutableListOf()

                    val list = ContactResDTO.toEntityList(response.body()!!, null)

                    val sortedByFriends = list.sortedByDescending { o -> o.statusFriend }
                    val exists = list.findLast { it.statusFriend }
                    val coincidences = list.find { !it.statusFriend }

                    if (exists != null) {
                        multableList.add(title1)
                        multableList.addAll(sortedByFriends)

                        val lastP = multableList.indexOf(exists)
                        if (coincidences != null)
                            multableList.add(
                                lastP + 1,
                                title2
                            )
                    } else {
                        if (sortedByFriends.isNotEmpty())
                            multableList.add(title2)
                        multableList.addAll(sortedByFriends)
                    }

                    _users.value = multableList

                } else {
                    _friendshipRequestWsError.value = context.getString(R.string.text_fail)
                }
            } catch (ex: Exception) {
                _friendshipRequestWsError.value = context.getString(R.string.text_fail)
            }
        }
    }

    override fun resetContacts() {
        _users.value = mutableListOf()
    }

    override fun getUsers(): List<Any>? {
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

    override fun sendFriendshipRequest(contact: ContactEntity) {
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

    override fun acceptOrRefuseRequest(contact: ContactEntity, state: Boolean) {
        contactEntity = contact
        isOffer = true
    }

    override fun validateIfExistsOffer() {
        if (isOffer) {
            //update item
            _updateItem.value = contactEntity
            isOffer = false
        }
    }
    //endregion
}
