package com.naposystems.pepito.utility.sharedViewModels.conversation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.pepito.entity.message.attachments.MediaStoreAudio
import com.naposystems.pepito.model.attachment.gallery.GalleryItem
import com.naposystems.pepito.utility.sharedViewModels.conversation.IContractConversationShareViewModel.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class ConversationShareViewModel @Inject constructor() : ViewModel(),
    IContractConversationShareViewModel, CameraAttachment, AudioAttachment, GalleryAttachment {

    private var base64: String = ""
    private var uri: String = ""
    private var _listMediaStoreAudio = mutableListOf<MediaStoreAudio>()
    private var _listGalleryItems = mutableListOf<GalleryItem>()

    private val _hasCameraSendClicked = MutableLiveData<Boolean>()
    val hasCameraSendClicked: LiveData<Boolean>
        get() = _hasCameraSendClicked

    private val _hasAudioSendClicked = MutableLiveData<Boolean>()
    val hasAudioSendClicked: LiveData<Boolean>
        get() = _hasAudioSendClicked

    private val _hasGallerySendClicked = MutableLiveData<Boolean>()
    val hasGallerySendClicked: LiveData<Boolean>
        get() = _hasGallerySendClicked

    private val _message = MutableLiveData<String>()
    val message: LiveData<String>
        get() = _message

    init {
        _hasCameraSendClicked.value = false
        _hasGallerySendClicked.value = false
        _message.value = ""
    }

    //region Implementation IContractConversationShareViewModel
    override fun setMessage(message: String) {
        _message.value = message
    }

    override fun resetMessage() {
        _message.value = ""
    }

    override fun setImageBase64(base64: String) {
        this.base64 = base64
    }

    override fun getImageBase64(): String = base64

    override fun setImageUri(uri: String) {
        this.uri = uri
    }

    override fun getImageUri(): String = uri
    //endregion

    //region Implementation IContractConversationShareViewModel.CameraAttachment
    override fun setCameraSendClicked() {
        _hasCameraSendClicked.value = true
    }

    override fun resetCameraSendClicked() {
        _hasCameraSendClicked.value = false
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

    //region Implementation IContractConversationShareViewModel.GalleryAttachment
    override fun setGalleryItemsSelected(listGalleryItem: List<GalleryItem>) {
        _listGalleryItems.clear()
        _listGalleryItems.addAll(listGalleryItem)
    }

    override fun getGalleryItemsSelected() = _listGalleryItems

    override fun setGallerySendClicked() {
        _hasGallerySendClicked.value = true
    }

    override fun resetGallerySendClicked() {
        _hasGallerySendClicked.value = false
    }
    //endregion
}