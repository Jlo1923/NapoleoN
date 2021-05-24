package com.naposystems.napoleonchat.ui.selfDestructTimeMessageNotSentFragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class SelfDestructTimeMessageNotSentViewDialogModel @Inject constructor(
    private val repository: SelfDestructTimeMessageNotSentDialogRepository
) : ViewModel() {

    private val _selfDestructTimeMessage = MutableLiveData<Int>()
    val selfDestructTimeMessage: LiveData<Int>
        get() = _selfDestructTimeMessage

    fun getSelfDestructTimeMessageNotSent() {
        viewModelScope.launch {
            _selfDestructTimeMessage.value = repository.getSelfDestructTimeMessageNotSent()
        }
    }

    fun setSelfDestructTimeMessageNotSent(time: Int) {
        viewModelScope.launch {
            repository.setSelfDestructTimeMessageNotSent(time)
            _selfDestructTimeMessage.value = time
        }
    }
}
