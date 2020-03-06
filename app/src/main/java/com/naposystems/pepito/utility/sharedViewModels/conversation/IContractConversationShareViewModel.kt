package com.naposystems.pepito.utility.sharedViewModels.conversation

import com.naposystems.pepito.entity.message.attachments.MediaStoreAudio
import com.naposystems.pepito.model.attachment.gallery.GalleryItem

interface IContractConversationShareViewModel {

    fun setMessage(message: String)
    fun resetMessage()
    fun setMediaBase64(base64: String)
    fun getImageBase64(): String
    fun setMediaUri(uri: String)
    fun getImageUri(): String
    fun setMediaThumbnailUri(uri: String)
    fun getMediaThumbnailUri(): String

    interface CameraAttachment {
        fun setCameraSendClicked()
        fun resetCameraSendClicked()
    }

    interface AudioAttachment {
        fun setAudiosSelected(listMediaStoreAudio: List<MediaStoreAudio>)
        fun getAudiosSelected(): List<MediaStoreAudio>
        fun setAudioSendClicked()
        fun resetAudioSendClicked()
    }

    interface GalleryAttachment {
        fun setGalleryItemsSelected(listGalleryItem: List<GalleryItem>)
        fun getGalleryItemsSelected(): List<GalleryItem>
        fun setGalleryTypeSelected(attachmentType: String)
        fun resetGalleryTypeSelected()
    }
}