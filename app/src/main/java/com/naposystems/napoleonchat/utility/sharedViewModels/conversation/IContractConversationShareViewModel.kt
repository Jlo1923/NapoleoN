package com.naposystems.napoleonchat.utility.sharedViewModels.conversation

import com.naposystems.napoleonchat.entity.message.attachments.Attachment
import com.naposystems.napoleonchat.entity.message.attachments.MediaStoreAudio
import com.naposystems.napoleonchat.model.emojiKeyboard.Emoji

interface IContractConversationShareViewModel {

    fun setMessage(message: String)
    fun getMessage(): String?
    fun resetMessage()
    fun setAttachmentSelected(attachment: Attachment)
    fun setAttachmentTaken(attachment: Attachment)
    fun resetAttachmentSelected()
    fun setGifSelected(attachment: Attachment)
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