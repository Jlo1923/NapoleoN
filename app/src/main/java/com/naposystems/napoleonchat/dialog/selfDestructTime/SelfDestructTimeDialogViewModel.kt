package com.naposystems.napoleonchat.dialog.selfDestructTime

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class SelfDestructTimeDialogViewModel
@Inject constructor(
    private val repository: SelfDestructTimeDialogRepository
) : ViewModel() {

    lateinit var getDestructTimeByContact: LiveData<Int>
    var selfDestructTimeByContact: Int? = -1

    private val _selfDestructTimeGlobal = MutableLiveData<Int>()
    val selfDestructTimeGlobal: LiveData<Int>
        get() = _selfDestructTimeGlobal

    private val _messageSelfDestructTimeNotSent = MutableLiveData<Int>()
    val messageSelfDestructTimeNotSent: LiveData<Int>
        get() = _messageSelfDestructTimeNotSent

    //region IContractSelfDestructTime.ViewMode
    fun getSelfDestructTime() {
        _selfDestructTimeGlobal.value = repository.getSelfDestructTime()
    }

    fun setSelfDestructTime(selfDestructTime: Int) {
        repository.setSelfDestructTime(selfDestructTime)
    }

    fun setSelfDestructTimeByContact(selfDestructTime: Int, contactId: Int) {
        viewModelScope.launch {
            repository.setSelfDestructTimeByContact(selfDestructTime, contactId)
        }
    }

    fun getSelfDestructTimeByContact(contactId: Int) {
        viewModelScope.launch {
            val selfDestructTime = repository.getSelfDestructTimeByContact(contactId)
            getDestructTimeByContact = selfDestructTime
        }
    }

    fun getMessageSelfDestructTimeNotSent() {
        _messageSelfDestructTimeNotSent.value = repository.getMessageSelfDestructTimeNotSent()
    }
    //endregion

}
