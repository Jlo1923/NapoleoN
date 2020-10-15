package com.naposystems.napoleonchat.ui.custom.inputPanel

import android.text.TextWatcher
import android.widget.ImageButton
import android.widget.TextView
import androidx.emoji.widget.EmojiAppCompatEditText
import com.naposystems.napoleonchat.entity.message.MessageAndAttachment

interface IContractInputPanel {
    fun setListener(listener: InputPanelWidget.Listener)
    fun isRecordingInLockedMode(): Boolean
    fun releaseRecordingLock()
    fun setEditTextWatcher(textWatcher: TextWatcher)
    fun getEditTex(): EmojiAppCompatEditText
    fun getImageButtonAttachment(): ImageButton
    fun getImageButtonCamera(): ImageButton
    fun getImageButtonEmoji(): ImageButton
    fun hideImageButtonCamera()
    fun showImageButtonCamera()
    fun hideImageButtonSend()
    fun showImageButtonSend()
    fun hideButtonRecord()
    fun showButtonRecord()
    fun openQuote(messageAndAttachment: MessageAndAttachment)
    fun containerWrap()
    fun containerNoWrap()
    fun getWebIdQuote(): String
    fun closeQuote()
    fun resetImage()
    fun getQuote(): MessageAndAttachment?
    fun setRecordingTime(time: Long)
    fun clearTextEditText()
    fun cancelRecording()
}