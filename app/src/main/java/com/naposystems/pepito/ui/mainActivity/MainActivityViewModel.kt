package com.naposystems.pepito.ui.mainActivity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.pepito.entity.User
import com.naposystems.pepito.repository.mainActivity.MainActivityRepository
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject

class MainActivityViewModel @Inject constructor(private val repository: MainActivityRepository) :
    ViewModel(), IContractMainActivity.ViewModel {

    private val _user = MutableLiveData<User>()
    val user: LiveData<User>
        get() = _user

    private val _errorGettingUser = MutableLiveData<Boolean>()
    val errorGettingUser: LiveData<Boolean>
        get() = _errorGettingUser

    private val _theme = MutableLiveData<Int>()
    val theme: LiveData<Int>
        get() = _theme

    init {
        _user.value = null
        _errorGettingUser.value = false
    }

    //region Implementation IContractMainActivity.ViewModel
    override fun getUser(firebaseId: String) {
        viewModelScope.launch {
            try {
                val localUser = repository.getUser(firebaseId)
                _user.value = localUser
            } catch (ex: Exception) {
                Timber.e(ex)
                _errorGettingUser.value = true
            }
        }
    }

    override fun getTheme() {
        viewModelScope.launch {
            _theme.value = repository.getTheme()
        }
    }

    //endregion
}