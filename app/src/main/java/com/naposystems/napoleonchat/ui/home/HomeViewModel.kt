package com.naposystems.napoleonchat.ui.home

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.napoleonchat.model.FriendShipRequest
import com.naposystems.napoleonchat.repository.contacts.ContactsRepository
import com.naposystems.napoleonchat.repository.home.HomeRepository
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.source.local.entity.MessageAttachmentRelation
import com.naposystems.napoleonchat.source.local.entity.UserEntity
import com.naposystems.napoleonchat.source.remote.dto.addContact.FriendshipRequestReceivedDTO
import com.naposystems.napoleonchat.utility.Constants.SharedPreferences.NAV_TO_CONTACTS
import com.naposystems.napoleonchat.utility.Constants.SharedPreferences.URIS_CACHE
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.utility.Utils
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class HomeViewModel
@Inject constructor(
    private val repository: HomeRepository,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val contactsRepository: ContactsRepository
) : ViewModel() {

    private lateinit var _userEntity: LiveData<UserEntity>
    val userEntity: LiveData<UserEntity>
        get() = _userEntity


    private lateinit var _conversations: LiveData<MutableList<MessageAttachmentRelation>>
    val conversations: LiveData<MutableList<MessageAttachmentRelation>>
        get() = _conversations

    private val _conversationsForSearch = MutableLiveData<List<MessageAttachmentRelation>>()
    val conversationsForSearch: LiveData<List<MessageAttachmentRelation>>
        get() = _conversationsForSearch

    private lateinit var _contacts: LiveData<MutableList<ContactEntity>>
    val contacts: LiveData<MutableList<ContactEntity>>
        get() = _contacts

    private val _contactsForSearch = MutableLiveData<List<ContactEntity>>()
    val contactsForSearch: LiveData<List<ContactEntity>>
        get() = _contactsForSearch


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

    private val _contact = MutableLiveData<ContactEntity>()
    val contact: LiveData<ContactEntity>
        get() = _contact

    var textBarSearch: String = ""

    init {

        _contact.value = null
        _jsonNotification.value = null
        _jsonCleaned.value = null
        _quantityFriendshipRequest.value = -1
    }

    //region Implementation IContractHome.ViewModel
    fun getFriendshipRequestHome() {
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


    fun resetDuplicates() {
        viewModelScope.launch {
            repository.deleteDuplicates()
            repository.addUUID()
        }
    }

    fun getConversation() {
        viewModelScope.launch {
            _conversations = repository.getMessagesForHome()
        }
    }

    fun getUserLiveData() {
        viewModelScope.launch {
            _userEntity = repository.getUserLiveData()
        }
    }

    fun getMessages() {
        viewModelScope.launch {
            try {
                repository.getRemoteMessages()
            } catch (ex: Exception) {
                Timber.e(ex)
            }
        }
    }

    fun getLocalContacts() {
        viewModelScope.launch {
            try {
                _contacts = contactsRepository.getLocalContacts()
            } catch (ex: Exception) {
                Timber.e(ex)
            }
        }
    }

    fun getDeletedMessages() {
        viewModelScope.launch {
            try {
                repository.getDeletedMessages()
            } catch (ex: Exception) {
                Timber.e(ex)
            }
        }
    }

    fun insertSubscription() {
        viewModelScope.launch {
            repository.insertSubscription()
        }
    }

    fun getFreeTrial(): Long {
        return repository.getFreeTrial()
    }

    fun getSubscriptionTime(): Long {
        return repository.getSubscriptionTime()
    }

    fun getJsonNotification() {
        _jsonNotification.value = repository.getJsonNotification()
    }

    fun getContact(contactId: Int) {
        viewModelScope.launch {
            _contact.value = repository.getContact(contactId)
        }
    }

    fun cleanJsonNotification(json: String) {
        viewModelScope.launch {
            repository.cleanJsonNotification()
            _jsonCleaned.value = json
        }
    }

    fun resetConversations() {
       //_conversations.value = listOf<MessageAttachmentRelation>()
    }

    fun cleanVariables() {
        _contact.value = null
        _jsonNotification.value = null
        _jsonCleaned.value = null
    }

    fun verifyMessagesToDelete() = viewModelScope.launch {
        repository.verifyMessagesToDelete()
    }

    fun getDialogSubscription(): Int {
        return repository.getDialogSubscription()
    }

    fun setDialogSubscription() {
        repository.setDialogSubscription()
    }

    //endregion

    private fun getFriendshipQuantity() {
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

    fun verifyMessagesReceived() {
        repository.verifyMessagesReceived()
    }

    fun verifyMessagesRead() {
        repository.verifyMessagesRead()
    }

    fun getPendingUris(): List<Uri> {
        val urisString = sharedPreferencesManager.getStringSet(URIS_CACHE)
        val listString = urisString?.toList()
        val listUris = listString?.map { Uri.parse(it) }
        return listUris ?: emptyList()
    }

    fun removePendingUris() = sharedPreferencesManager.puStringSet(URIS_CACHE, emptyList())

    fun markGoToContacts() = sharedPreferencesManager.putBoolean(NAV_TO_CONTACTS, false)

    fun isMarkGoToContacts() = sharedPreferencesManager.getBoolean(NAV_TO_CONTACTS, false)

    fun lastSubscription() = viewModelScope.launch {
        try{
            repository.lastSubscription()
        }catch (ex: Exception){
            Timber.e(ex)
        }
    }


    fun searchContact(query: String) {
        viewModelScope.launch {
            try {
                _contactsForSearch.value = _contacts.value?.filter {
                    Utils.validateNickname(it, query) || Utils.validateDisplayName(it, query)
                }
            } catch (ex: Exception) {
                Timber.e(ex)
            }
        }
    }

    fun setTextSearch(text: String) {
        textBarSearch = text
    }

    fun getTextSearch() = textBarSearch

    fun resetTextSearch() {
        textBarSearch = ""
    }

}
