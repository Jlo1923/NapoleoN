package com.naposystems.napoleonchat.ui.selfDestructTime

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class SelfDestructTimeViewModel @Inject constructor(private val repository: IContractSelfDestructTime.Repository) :
    ViewModel(), IContractSelfDestructTime.ViewModel {

    lateinit var getDestructTimeByContact: LiveData<Int>
    var selfDestructTimeByContact: Int? = -1

    private val _selfDestructTimeGlobal = MutableLiveData<Int>()
    val selfDestructTimeGlobal: LiveData<Int>
        get() = _selfDestructTimeGlobal

    private val _messageSelfDestructTimeNotSent = MutableLiveData<Int>()
    val messageSelfDestructTimeNotSent: LiveData<Int>
        get() = _messageSelfDestructTimeNotSent

    //region IContractSelfDestructTime.ViewMode
    override fun getSelfDestructTime() {
        _selfDestructTimeGlobal.value = repository.getSelfDestructTime()
    }

    override fun setSelfDestructTime(selfDestructTime: Int) {
        repository.setSelfDestructTime(selfDestructTime)
    }

    override fun setSelfDestructTimeByContact(selfDestructTime: Int, contactId: Int) {
        viewModelScope.launch {
            repository.setSelfDestructTimeByContact(selfDestructTime, contactId)
        }
    }

    override fun getSelfDestructTimeByContact(contactId: Int) {
        viewModelScope.launch {
            val selfDestructTime = repository.getSelfDestructTimeByContact(contactId)
            getDestructTimeByContact = selfDestructTime
        }
    }

    override fun getMessageSelfDestructTimeNotSent() {
        _messageSelfDestructTimeNotSent.value = repository.getMessageSelfDestructTimeNotSent()
    }
    //endregion

}
