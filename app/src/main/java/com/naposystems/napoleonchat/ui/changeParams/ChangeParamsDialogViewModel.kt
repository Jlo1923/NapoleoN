package com.naposystems.napoleonchat.ui.changeParams

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.napoleonchat.R
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChangeParamsDialogViewModel @Inject constructor(
    private val context: Context,
    private val repository: IContractChangeDialogParams.Repository
) : ViewModel(), IContractChangeDialogParams.ViewModel {

    private val _responseEditFake = MutableLiveData<Boolean>()
    val responseEditFake: LiveData<Boolean>
        get() = _responseEditFake

    private val _changeParamsWsError = MutableLiveData<String>()
    val changeParamsWsError: LiveData<String>
        get() = _changeParamsWsError


    override fun updateNameFakeContact(contactId: Int, nameFake: String) {

        viewModelScope.launch {
            try {
                val response = repository.updateNameOrNickNameFakeContact(contactId, nameFake, true)
                if (response.isSuccessful) {
                    response.body()?.let { repository.updateContactFakeLocal(contactId, it) }
                    _responseEditFake.value = true
                }

            } catch (ex: Exception) {
                ex.printStackTrace()
                _changeParamsWsError.value = context.getString(R.string.text_fail)
            }
        }
    }

    override fun updateNicknameFakeContact(contactId: Int, nicknameFake: String) {
        viewModelScope.launch {
            try {

                val response =
                    repository.updateNameOrNickNameFakeContact(contactId, nicknameFake, false)
                if (response.isSuccessful) {
                    response.body()?.let { repository.updateContactFakeLocal(contactId, it) }
                    _responseEditFake.value = true
                }

            } catch (ex: Exception) {
                _changeParamsWsError.value = context.getString(R.string.text_fail)
            }
        }
    }

}
