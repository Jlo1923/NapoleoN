package com.naposystems.napoleonchat.ui.logout

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class LogoutDialogViewModel @Inject constructor(private val repository: IContractLogout.Repository) :
    ViewModel(), IContractLogout.ViewModel {

    private val _logoutStatus = MutableLiveData<Int>()
    val logoutStatus: LiveData<Int>
        get() = _logoutStatus

    override fun logOut() {
        viewModelScope.launch {
            try {
                _logoutStatus.value = LogoutDialogFragment.IS_SERVICE_CALLED
                val response = repository.logOut()

                if (response.isSuccessful) {
                    repository.clearData()
                    _logoutStatus.value = LogoutDialogFragment.SERVICE_ANSWER_OK
                } else {
                    _logoutStatus.value = LogoutDialogFragment.SERVICE_ANSWER_ERROR
                }
            } catch (e: Exception) {
                _logoutStatus.value = LogoutDialogFragment.SERVICE_ANSWER_ERROR
                Timber.e(e)
            }
        }
    }
}