package com.naposystems.pepito.ui.previewImageSend

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject

class SharePreviewImageSendViewModel @Inject constructor() : ViewModel(), IContractPreviewImageSend.ViewModel {

    private val _hasCancelClicked = MutableLiveData<Boolean>()
    val hasCancelClicked: LiveData<Boolean>
        get() = _hasCancelClicked

    private val _message = MutableLiveData<String>()
    val message: LiveData<String>
        get() = _message

    init {
        _hasCancelClicked.value = false
        _message.value = ""
    }

    //region Implementation IContractPreviewImageSend.ViewModel

    override fun setCancelClicked() {
        _hasCancelClicked.value = true
    }

    override fun setMessage(message: String) {
        _message.value = message
    }

    override fun resetMessage() {
        _message.value = ""
    }

    override fun resetCancelClicked() {
        _hasCancelClicked.value = false
    }

    //endregion
}
