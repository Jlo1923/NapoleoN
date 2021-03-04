package com.naposystems.napoleonchat.ui.custom.inputPanel

import android.text.TextWatcher
import android.widget.ImageButton
import androidx.emoji.widget.EmojiAppCompatEditText
import com.naposystems.napoleonchat.source.local.entity.MessageAttachmentRelation

interface IContractInputPanel {
    fun setListener(listener: InputPanelWidget.Listener)
    fun isRecordingInLockedMode(): Boolean
    fun releaseRecordingLock()
    fun setEditTextWatcher(textWatcher: TextWatcher)
    fun getEditText(): EmojiAppCompatEditText
    fun getImageButtonAttachment(): ImageButton
    fun getImageButtonCamera(): ImageButton
    fun getImageButtonEmoji(): ImageButton
    fun hideImageButtonCamera()
    fun showImageButtonCamera()
    fun hideImageButtonSend()
    fun showImageButtonSend()
    fun hideButtonRecord()
    fun showButtonRecord()
    fun openQuote(messageAndAttachmentRelation: MessageAttachmentRelation)
    fun containerWrap()
    fun containerNoWrap()
    fun getWebIdQuote(): String
    fun closeQuote()
    fun resetImage()
    fun getQuote(): MessageAttachmentRelation?
    fun setRecordingTime(time: Long)
    fun clearTextEditText()
    fun cancelRecording()
}