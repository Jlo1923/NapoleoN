package com.naposystems.napoleonchat.ui.contactUs

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.napoleonchat.repository.contactUs.ContactUsRepository
import com.naposystems.napoleonchat.source.remote.dto.contactUs.ContactUsReqDTO
import com.naposystems.napoleonchat.utility.Constants
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class ContactUsViewModel
@Inject constructor(
    private val repository: ContactUsRepository
) : ViewModel() {

    private val _pqrsCreatedSuccessfully = MutableLiveData<Boolean>()
    val pqrsCreatedSuccessfully: LiveData<Boolean>
        get() = _pqrsCreatedSuccessfully

    private val _pqrsCreatingErrors = MutableLiveData<List<String>>()
    val pqrsCreatingErrors: LiveData<List<String>>
        get() = _pqrsCreatingErrors

    //region Implementation IContractContactUs.ViewModel
    fun sendPqrs(contactUsReqDTO: ContactUsReqDTO) {
        viewModelScope.launch {
            try {
                val response = repository.sendPqrs(contactUsReqDTO)

                if (response.isSuccessful) {
                    _pqrsCreatedSuccessfully.value = true
                } else {
                    when (response.code()) {
                        Constants.CodeHttp.UNPROCESSABLE_ENTITY.code -> {
                            _pqrsCreatedSuccessfully.value = false
                            _pqrsCreatingErrors.value =
                                repository.getUnprocessableEntityError(response)
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
