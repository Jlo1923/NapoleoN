package com.naposystems.pepito.ui.conversationCamera

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject

class ShareConversationCameraViewModel @Inject constructor() : ViewModel(),
    IContractConversationCamera.ViewModel {

    private var base64: String = ""
    private var uri: String = ""

    private val _hasSendClicked = MutableLiveData<Boolean>()
    val hasSendClicked: LiveData<Boolean>
        get() = _hasSendClicked

    private val _message = MutableLiveData<String>()
    val message: LiveData<String>
        get() = _message

    init {
        _hasSendClicked.value = false
        _message.value = ""
    }

    //region Implementation IContractPreviewImageSend.ViewModel

    override fun setSendClicked() {
        _hasSendClicked.value = true
    }

    override fun setMessage(message: String) {
        _message.value = message
    }

    override fun resetMessage() {
        _message.value = ""
    }

    override fun resetSendClicked() {
        _hasSendClicked.value = false
    }

    override fun setBase64(base64: String) {
        this.base64 = base64
    }

    override fun getBase64(): String = base64

    override fun setUri(uri: String) {
        this.uri = uri
    }

    override fun getUri(): String = uri

    //endregion
}
