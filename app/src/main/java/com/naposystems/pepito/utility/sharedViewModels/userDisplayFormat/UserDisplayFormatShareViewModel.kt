package com.naposystems.pepito.utility.sharedViewModels.userDisplayFormat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.naposystems.pepito.repository.userDisplayFormat.UserDisplayFormatRepository
import com.naposystems.pepito.ui.userDisplayFormat.IContractUserDisplayFormat
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

    override fun getUserDisplayFormat() {
        _userDisplayFormat.value = repository.getUserDisplayFormat()
    }

    override fun getValUserDisplayFormat(): Int? {
        return userDisplayFormat.value
    }
    //endregion
}
