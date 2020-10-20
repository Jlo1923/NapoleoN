package com.naposystems.napoleonchat.utility.sharedViewModels.userDisplayFormat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.naposystems.napoleonchat.repository.userDisplayFormat.UserDisplayFormatRepository
import com.naposystems.napoleonchat.ui.userDisplayFormat.IContractUserDisplayFormat
import javax.inject.Inject

class UserDisplayFormatShareViewModel @Inject constructor(
    private val repository: UserDisplayFormatRepository
) : ViewModel(), IContractUserDisplayFormat.ViewModel {

    private val _userDisplayFormat = MutableLiveData<Int>()
    val userDisplayFormat: LiveData<Int>
        get() = _userDisplayFormat

    //region Implementation IContractUserDisplayFormat.ViewModel
    override fun setUserDisplayFormat(format: Int) {
        _userDisplayFormat.value = format
        repository.setUserDisplayFormat(format)
    }

    override fun getUserDisplayFormat() : Int {
        val response = repository.getUserDisplayFormat()
        _userDisplayFormat.value = response
        return response
    }
    //endregion
}
