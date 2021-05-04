package com.naposystems.napoleonchat.ui.dialog.userDisplayFormat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject

class UserDisplayFormatDialogViewModel
@Inject constructor(
    private val repository: UserDisplayFormatDialogRepository
) : ViewModel() {

    private val _userDisplayFormat = MutableLiveData<Int>()
    val userDisplayFormat: LiveData<Int>
        get() = _userDisplayFormat

    //region Implementation IContractUserDisplayFormat.ViewModel
    fun setUserDisplayFormat(format: Int) {
        _userDisplayFormat.value = format
        repository.setUserDisplayFormat(format)
    }

    fun getUserDisplayFormat(): Int {
        val response = repository.getUserDisplayFormat()
        _userDisplayFormat.value = response
        return response
    }
    //endregion
}
