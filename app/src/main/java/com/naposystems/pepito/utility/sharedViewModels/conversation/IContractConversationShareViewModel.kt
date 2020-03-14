package com.naposystems.pepito.utility.sharedViewModels.conversation

import com.naposystems.pepito.entity.message.attachments.Attachment
import com.naposystems.pepito.entity.message.attachments.MediaStoreAudio
import com.naposystems.pepito.model.attachment.gallery.GalleryItem

interface IContractConversationShareViewModel {

    fun setMessage(message: String)
    fun getMessage(): String?
    fun resetMessage()
    fun setAttachmentSelected(attachment: Attachment)
    fun resetAttachmentSelected()

    interface AudioAttachment {
        fun setAudiosSelected(listMediaStoreAudio: List<MediaStoreAudio>)
        fun getAudiosSelected(): List<MediaStoreAudio>
        fun setAudioSendClicked()
        fun resetAudioSendClicked()
    }
}