package com.naposystems.napoleonchat.ui.contacts

import androidx.lifecycle.*
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.utility.Utils
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class ContactsViewModel @Inject constructor(private val repository: IContractContacts.Repository) :
    ViewModel(), IContractContacts.ViewModel {

    private lateinit var _contacts: LiveData<MutableList<ContactEntity>>
    val contacts: LiveData<MutableList<ContactEntity>>
        get() = _contacts

    private val _contactsForSearch = MutableLiveData<List<ContactEntity>>()
    val contactsForSearch: LiveData<List<ContactEntity>>
        get() = _contactsForSearch

    private val _webServiceErrors = MutableLiveData<List<String>>()
    val webServiceErrors: LiveData<List<String>>
        get() = _webServiceErrors

    private val _contactsLoaded = MutableLiveData<Boolean>()
    val contactsLoaded: LiveData<Boolean>
        get() = _contactsLoaded

    var textBarSearch: String = ""

    init {
        _contactsLoaded.value = false
    }

    //region Implementation IContractContacts.ViewModel

    override fun getLocalContacts() {
        viewModelScope.launch {
            try {
                _contacts = repository.getLocalContacts()
                _contactsLoaded.value = true
            } catch (ex: Exception) {
                Timber.e(ex)
            }
        }
    }

    override fun searchContact(query: String) {
        viewModelScope.launch {
            try {
                _contactsForSearch.value = _contacts.value!!.filter {
                    Utils.validateNickname(it, query) || Utils.validateDisplayName(it, query)
                }
            } catch (ex: Exception) {
                Timber.e(ex)
            }
        }
    }

    override fun setTextSearch(text: String) {
        textBarSearch = text
    }

    override fun getTextSearch() = textBarSearch

    override fun resetTextSearch() {
        textBarSearch = ""
    }

    override fun resetContactsLoaded() {
        _contactsLoaded.value = false
    }

    //endregion
}
