package com.naposystems.pepito.ui.selfDestructTime

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class SelfDestructTimeViewModel @Inject constructor(private val repository: IContractSelfDestructTime.Repository) :
    ViewModel(), IContractSelfDestructTime.ViewModel {

    private val _selfDestructTime = MutableLiveData<Int>()
    val selfDestructTime: LiveData<Int>
        get() = _selfDestructTime

    lateinit var getDestructTimeByContact: LiveData<Int>
    var selfDestructTimeByContact: Int? = -1

    var selfDestructTimeGlobal: Int = 0

    init {
        _selfDestructTime.value = null
    }

    //region IContractSelfDestructTime.ViewMode
    override fun getSelfDestructTime() {
        val selfDestructTime = repository.getSelfDestructTime()
        _selfDestructTime.value = selfDestructTime
        selfDestructTimeGlobal = selfDestructTime
    }

    override fun setSelfDestructTime(selfDestructTime: Int) {
        _selfDestructTime.value = selfDestructTime
        repository.setSelfDestructTime(selfDestructTime)
    }

    override fun setSelfDestructTimeByContact(selfDestructTime: Int, contactId : Int) {
        viewModelScope.launch {
            repository.setSelfDestructTimeByContact(selfDestructTime, contactId)
        }
    }

    override fun getSelfDestructTimeByContact(contactId: Int) {
        viewModelScope.launch {
            val selfDestructTime= repository.getSelfDestructTimeByContact(contactId)
            getDestructTimeByContact = selfDestructTime
        }
    }
    //endregion

}
