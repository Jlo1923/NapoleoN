package com.naposystems.pepito.ui.contactUs

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.pepito.dto.contactUs.ContactUsReqDTO
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class ContactUsViewModel @Inject constructor(private val repository: IContractContactUs.Repository) :
    ViewModel(), IContractContactUs.ViewModel {

    private val _pqrsCreatedSuccessfully = MutableLiveData<Boolean>()
    val pqrsCreatedSuccessfully: LiveData<Boolean>
        get() = _pqrsCreatedSuccessfully

    private val _pqrsCreatingErrors = MutableLiveData<List<String>>()
    val pqrsCreatingErrors: LiveData<List<String>>
        get() = _pqrsCreatingErrors

    //region Implementation IContractContactUs.ViewModel
    override fun sendPqrs(contactUsReqDTO: ContactUsReqDTO) {
        viewModelScope.launch {
            try {
                val response = repository.sendPqrs(contactUsReqDTO)

                if (response.isSuccessful) {
                    _pqrsCreatedSuccessfully.value = true
                } else {
                    when (response.code()) {
                        422 -> {
                            _pqrsCreatedSuccessfully.value = false
                            _pqrsCreatingErrors.value = repository.get422Error(response)
                        }
                        else -> {
                            _pqrsCreatedSuccessfully.value = false
                            _pqrsCreatingErrors.value = repository.getDefaultError(response)
                        }
                    }
                }
            } catch (e: Exception) {
                _pqrsCreatedSuccessfully.value = false
                Timber.e(e)
            }
        }
    }
    //endregion
}
