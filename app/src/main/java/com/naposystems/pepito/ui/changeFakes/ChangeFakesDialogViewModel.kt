package com.naposystems.pepito.ui.changeFakes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChangeFakesDialogViewModel @Inject constructor(
    private val repository: IContractChangeFakes.Repository
) : ViewModel(), IContractChangeFakes.ViewModel {

    private val _responseEditFake = MutableLiveData<Boolean>()
    val responseEditFake: LiveData<Boolean>
        get() = _responseEditFake

    override fun updateNameFakeContact(contactId: Int, nameFake: String) {
        viewModelScope.launch {
            if (nameFake.isEmpty()) {
                repository.updateNameFakeContact(contactId, " ")
            } else {
                repository.updateNameFakeContact(contactId, nameFake)
            }
            _responseEditFake.value = true
        }
    }

    override fun updateNicknameFakeContact(contactId: Int, nicknameFake: String) {
        viewModelScope.launch {
            repository.updateNicknameFakeContact(contactId, nicknameFake)
            _responseEditFake.value = true
        }
    }

}
