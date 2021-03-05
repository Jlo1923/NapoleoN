package com.naposystems.napoleonchat.utility.sharedViewModels.conversation

import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.model.MediaStoreAudio
import com.naposystems.napoleonchat.model.emojiKeyboard.Emoji

interface IContractConversationShareViewModel {

    fun setMessage(message: String)
    fun getMessage(): String?
    fun resetMessage()
    fun setAttachmentSelected(attachmentEntity: AttachmentEntity)
    fun setAttachmentTaken(attachmentEntity: AttachmentEntity)
    fun resetAttachmentSelected()
    fun setGifSelected(attachmentEntity: AttachmentEntity)
    fun resetGifSelected()
    fun resetAttachmentTaken()
    fun getQuoteWebId(): String?
    fun setQuoteWebId(webId: String)
    fun resetQuoteWebId()
    fun setEmojiSelected(emoji : Emoji)
    fun resetEmojiSelected()

    interface AudioAttachment {
        fun setAudiosSelected(listMediaStoreAudio: List<MediaStoreAudio>)
        fun getAudiosSelected(): List<MediaStoreAudio>
        fun setAudioSendClicked()
        fun resetAudioSendClicked()
    }
}