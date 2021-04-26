package com.naposystems.napoleonchat.utility.sharedViewModels.conversation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.napoleonchat.model.MediaStoreAudio
import com.naposystems.napoleonchat.model.emojiKeyboard.Emoji
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import kotlinx.coroutines.launch
import javax.inject.Inject

class ConversationSharedViewModel
@Inject constructor() : ViewModel(), AudioAttachment {

    private var _listMediaStoreAudio = mutableListOf<MediaStoreAudio>()

    private val _attachmentSelected = MutableLiveData<AttachmentEntity>()
    val attachmentEntitySelected: LiveData<AttachmentEntity>
        get() = _attachmentSelected

    private val _listAttachments = MutableLiveData<List<AttachmentEntity>>()
    val listAttachments: LiveData<List<AttachmentEntity>>
        get() = _listAttachments

    private val _attachmentTaken = MutableLiveData<AttachmentEntity>()
    val attachmentEntityTaken: LiveData<AttachmentEntity>
        get() = _attachmentTaken

    private val _emojiSelected = MutableLiveData<Emoji>()
    val emojiSelected: LiveData<Emoji>
        get() = _emojiSelected

    private val _hasAudioSendClicked = MutableLiveData<Boolean>()
    val hasAudioSendClicked: LiveData<Boolean>
        get() = _hasAudioSendClicked

    private val _gifSelected = MutableLiveData<AttachmentEntity>()
    val gifSelected: LiveData<AttachmentEntity>
        get() = _gifSelected

    private val _message = MutableLiveData<String>()
    val message: LiveData<String>
        get() = _message

    private val _quoteWebId = MutableLiveData<String>()
    val quoteWebId: LiveData<String>
        get() = _quoteWebId

    init {
        _message.value = ""
    }

    //region Implementation IContractConversationShareViewModel
    fun setMessage(message: String) {
        _message.value = message
    }

    fun getMessage() = _message.value

    fun resetMessage() {
        _message.value = ""
    }

    fun setAttachmentSelected(attachmentEntity: AttachmentEntity) {
        _attachmentSelected.value = attachmentEntity
    }

    fun setAttachmentTaken(attachmentEntity: AttachmentEntity) {
        _attachmentTaken.value = attachmentEntity
    }

    fun resetAttachmentSelected() {
        _attachmentSelected.value = null
    }

    fun setGifSelected(attachmentEntity: AttachmentEntity) {
        this._gifSelected.value = attachmentEntity
    }

    fun resetGifSelected() {
        this._gifSelected.value = null
    }

    fun resetAttachmentTaken() {
        _attachmentTaken.value = null
    }

    fun getQuoteWebId() = this._quoteWebId.value

    fun setQuoteWebId(webId: String) {
        this._quoteWebId.value = webId
    }

    fun resetQuoteWebId() {
        this._quoteWebId.value = null
    }

    fun setEmojiSelected(emoji: Emoji) {
        _emojiSelected.value = emoji
    }

    fun resetEmojiSelected() {
        _emojiSelected.value = null
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