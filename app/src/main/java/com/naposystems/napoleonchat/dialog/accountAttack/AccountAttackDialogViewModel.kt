package com.naposystems.napoleonchat.dialog.accountAttack

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class AccountAttackDialogViewModel @Inject constructor(
    private val repository: AccountAttackDialogRepository
) : ViewModel() {

    private val _closeModal = MutableLiveData<Boolean>()
    val closeModal: LiveData<Boolean>
        get() = _closeModal

    init {
        _closeModal.value = false
    }

    fun blockAttack() {
        viewModelScope.launch {
            try {
                val response = repository.blockAttack()

                if (response.isSuccessful) {
                    repository.resetExistingAttack()
                    _closeModal.value = true
                } else {
                    _closeModal.value = false
                    Timber.e("Error al bloquear!!")
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    fun resetExistingAttack() {
        viewModelScope.launch {
            repository.resetExistingAttack()
        }
    }
}
