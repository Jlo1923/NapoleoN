package com.naposystems.napoleonchat.ui.mainActivity

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.napoleonchat.repository.mainActivity.MainActivityRepository
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.source.local.entity.UserEntity
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class MainActivityViewModel
@Inject constructor(
    private val repository: MainActivityRepository
) : ViewModel(), IContractMainActivity.ViewModel {

    private var callChannel = ""
    private var isVideoCall: Boolean? = null

    private val _user = MutableLiveData<UserEntity>()
    val userEntity: LiveData<UserEntity>
        get() = _user

    private val _errorGettingUser = MutableLiveData<Boolean>()
    val errorGettingUser: LiveData<Boolean>
        get() = _errorGettingUser

    private val _accountStatus = MutableLiveData<Int>()
    val accountStatus: LiveData<Int>
        get() = _accountStatus

    private val _contact = MutableLiveData<ContactEntity>()
    val contact: LiveData<ContactEntity>
        get() = _contact

    init {
        _user.value = null
        _errorGettingUser.value = false
    }

    //region Implementation IContractMainActivity.ViewModel
    override fun getUser() {
        viewModelScope.launch {
            try {
                val localUser = repository.getUser()
                _user.value = localUser
            } catch (ex: Exception) {
                Timber.e(ex)
                _errorGettingUser.value = true
            }
        }
    }

    override fun getAccountStatus() {
        viewModelScope.launch {
            _accountStatus.value = repository.getAccountStatus()
        }
    }

    override fun getOutputControl(): Int {
        return repository.getOutputControl()
    }

    override fun setOutputControl(state: Int) {
        viewModelScope.launch {
            repository.setOutputControl(state)
        }
    }

    override fun getTimeRequestAccessPin(): Int {
        return repository.getTimeRequestAccessPin()
    }

    override fun setLockTimeApp() {
        viewModelScope.launch {
            val timeRequestAccessPin = repository.getTimeRequestAccessPin()
            val currentTime = System.currentTimeMillis()
            val blockTime = currentTime.plus(timeRequestAccessPin)
            repository.setLockTimeApp(blockTime)
        }
    }

    override fun setLockStatus(state: Int) {
        viewModelScope.launch {
            repository.setLockStatus(state)
        }
    }

    override fun setJsonNotification(json: String) {
        viewModelScope.launch {
            repository.setJsonNotification(json)
        }
    }

    override fun getLockTimeApp(): Long {
        var lockTime = 0L
        viewModelScope.launch {
            lockTime = repository.getLockTimeApp()
        }
        return lockTime
    }

    override fun getContact(contactId: Int) {
        viewModelScope.launch {
            _contact.value = repository.getContactById(contactId)
        }
    }

    override fun resetContact() {
        _contact.value = null
    }

    override fun getCallChannel() = this.callChannel

    override fun setCallChannel(channel: String) {
        callChannel = channel
    }

    override fun resetCallChannel() {
        callChannel = ""
    }

    override fun setIsVideoCall(isVideoCall: Boolean) {
        this.isVideoCall = isVideoCall
    }

    override fun isVideoCall() = isVideoCall

    override fun resetIsVideoCall() {
        isVideoCall = null
    }

    override fun getRecoveryQuestionsPref(): Int {
        return repository.getRecoveryQuestionsPref()
    }

    override fun disconnectSocket() {
//        repository.disconnectSocket()
    }

    fun addUriListToCache(listOf: List<Uri>) {
        repository.addUriListToCache(listOf)
    }
}