package com.naposystems.pepito.ui.selfDestructTime

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject

class SelfDestructTimeViewModel @Inject constructor(private val repository: IContractSelfDestructTime.Repository) :
    ViewModel(), IContractSelfDestructTime.ViewModel {

    private val _selfDestructTime = MutableLiveData<Int>()
    val selfDestructTime: LiveData<Int>
        get() = _selfDestructTime

    init {
        _selfDestructTime.value = null
    }

    //region IContractSelfDestructTime.ViewMode
    override fun getSelfDestructTime() {
        _selfDestructTime.value = repository.getSelfDestructTime()
    }

    override fun setSelfDestructTime(selfDestructTime: Int) {
        _selfDestructTime.value = selfDestructTime
        repository.setSelfDestructTime(selfDestructTime)
    }
    //endregion
}
