package com.naposystems.pepito.ui.register.accessPin

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.pepito.R
import com.naposystems.pepito.dto.accessPin.CreateAccountReqDTO
import com.naposystems.pepito.entity.User
import com.naposystems.pepito.repository.accessPin.CreateAccountRepository
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject

class AccessPinViewModel @Inject constructor(
    private val context: Context,
    private val repository: CreateAccountRepository
) :
    ViewModel(), IContractAccessPin.ViewModel {

    val accessPin = MutableLiveData<String>()
    val confirmAccessPin = MutableLiveData<String>()

    private val _webServiceError = MutableLiveData<List<String>>()
    val webServiceError: LiveData<List<String>>
        get() = _webServiceError

    private val _userCreationError = MutableLiveData<String>()
    val userCreationError: LiveData<String>
        get() = _userCreationError

    private val _userCreatedSuccessfully = MutableLiveData<Boolean>()
    val userCreatedSuccessfully: LiveData<Boolean>
        get() = _userCreatedSuccessfully

    private val _userCreatedLocallySuccessfully = MutableLiveData<Boolean>()
    val userCreatedLocallySuccessfully: LiveData<Boolean>
        get() = _userCreatedLocallySuccessfully

    private val _openHomeFragment = MutableLiveData<Boolean>()
    val openHomeFragment: LiveData<Boolean>
        get() = _openHomeFragment

    init {
        accessPin.value = ""
        confirmAccessPin.value = ""
        _webServiceError.value = ArrayList()
        _userCreatedSuccessfully.value = null
        _userCreatedLocallySuccessfully.value = null
        _openHomeFragment.value = null
    }

    fun openHomeFragment() {
        _openHomeFragment.value = true
    }

    fun onOpenedHomeFragment() {
        _openHomeFragment.value = null
    }

    //region Implementation IContractAccessPin.ViewModel
    override fun createAccount(createAccountReqDTO: CreateAccountReqDTO) {
        viewModelScope.launch {
            try {
                val response = repository.createAccount(createAccountReqDTO)

                if (response.isSuccessful) {
                    _userCreatedSuccessfully.value = true
                } else {
                    when (response.code()) {
                        422 -> _webServiceError.value = repository.get422Error(response)
                        else -> _webServiceError.value = repository.getError(response)
                    }
                }
            } catch (ex: Exception) {
                Timber.d(ex)
                val error = context.getString(R.string.something_went_wrong)
                _webServiceError.value = arrayListOf(error)
            }
        }
    }

    override fun createUser(user: User) {
        viewModelScope.launch {
            try {
                repository.createUser(user)
                _userCreatedLocallySuccessfully.value = true
            } catch (ex: Exception) {
                Timber.d(ex)
                val error = context.getString(R.string.something_went_wrong)
                _userCreationError.value = error
            }
        }
    }

    //endregion
}
