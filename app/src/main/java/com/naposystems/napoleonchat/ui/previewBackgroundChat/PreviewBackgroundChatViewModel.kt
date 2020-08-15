package com.naposystems.napoleonchat.ui.previewBackgroundChat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.napoleonchat.repository.previewBackgrounChat.PreviewBackgroundChatRepository
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class PreviewBackgroundChatViewModel@Inject constructor(
    private val repository: PreviewBackgroundChatRepository
) : ViewModel(), IContractPreviewBackgroundChat.ViewModel {

    private val _chatBackgroundUpdated = MutableLiveData<Boolean>()
    val chatBackgroundUpdated: LiveData<Boolean>
        get() = _chatBackgroundUpdated

    private val _chatBackground = MutableLiveData<String>()
    val chatBackground: LiveData<String>
        get() = _chatBackground

    init {
        _chatBackgroundUpdated.value = null
        _chatBackground.value = null
    }

    fun resetChatBackgroundUpdated() {
        _chatBackgroundUpdated.value = null
    }

    fun resetChatBackground() {
        _chatBackground.value = null
    }

    fun setChatBackground (uri: String) {
        _chatBackground.value = uri
    }

    override fun updateChatBackground(uri: String) {
        viewModelScope.launch {
            try {
                repository.updateChatBackground(uri)
                _chatBackgroundUpdated.value = true
            } catch (e: Exception) {
                _chatBackgroundUpdated.value = false
                Timber.e(e)
            }
        }
    }
}
