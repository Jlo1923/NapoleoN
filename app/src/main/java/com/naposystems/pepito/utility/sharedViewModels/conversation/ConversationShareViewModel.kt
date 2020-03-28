package com.naposystems.pepito.utility.sharedViewModels.conversation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.pepito.entity.message.attachments.Attachment
import com.naposystems.pepito.entity.message.attachments.MediaStoreAudio
import com.naposystems.pepito.utility.sharedViewModels.conversation.IContractConversationShareViewModel.AudioAttachment
import kotlinx.coroutines.launch
import javax.inject.Inject

class ConversationShareViewModel @Inject constructor() : ViewModel(),
    IContractConversationShareViewModel, AudioAttachment {

    private var _listMediaStoreAudio = mutableListOf<MediaStoreAudio>()

    private val _attachmentSelected = MutableLiveData<Attachment>()
    val attachmentSelected: LiveData<Attachment>
        get() = _attachmentSelected

    private val _hasAudioSendClicked = MutableLiveData<Boolean>()
    val hasAudioSendClicked: LiveData<Boolean>
        get() = _hasAudioSendClicked

    private val _gifSelected = MutableLiveData<Attachment>()
    val gifSelected: LiveData<Attachment>
        get() = _gifSelected

    private val _message = MutableLiveData<String>()
    val message: LiveData<String>
        get() = _message

    init {
        _message.value = ""
    }

    //region Implementation IContractConversationShareViewModel
    override fun setMessage(message: String) {
        _message.value = message
    }

    override fun getMessage() = _message.value

    override fun resetMessage() {
        _message.value = ""
    }

    override fun setAttachmentSelected(attachment: Attachment) {
        _attachmentSelected.value = attachment
    }

    override fun resetAttachmentSelected() {
        _attachmentSelected.value = null
    }

    override fun setGifSelected(attachment: Attachment) {
        this._gifSelected.value = attachment
    }

    override fun resetGifSelected() {
        this._gifSelected.value = null
    }

    //endregion

    //region Implementation IContractConversationShareViewModel.AudioAttachment
    override fun setAudiosSelected(listMediaStoreAudio: List<MediaStoreAudio>) {
        viewModelScope.launch {
            _listMediaStoreAudio.clear()
            _listMediaStoreAudio.addAll(listMediaStoreAudio)
        }
    }

    override fun getAudiosSelected() = this._listMediaStoreAudio

    override fun setAudioSendClicked() {
        _hasAudioSendClicked.value = true
    }

    override fun resetAudioSendClicked() {
        _hasAudioSendClicked.value = false
    }
    //endregion
}