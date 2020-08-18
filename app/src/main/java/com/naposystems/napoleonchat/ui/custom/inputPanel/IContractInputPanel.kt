package com.naposystems.napoleonchat.ui.custom.inputPanel

import android.text.TextWatcher
import android.widget.ImageButton
import android.widget.TextView
import androidx.emoji.widget.EmojiAppCompatEditText
import com.naposystems.napoleonchat.entity.message.MessageAndAttachment

interface IContractInputPanel {
    fun setEditTextWatcher(textWatcher: TextWatcher)
    fun getEditTex(): EmojiAppCompatEditText
    fun getImageButtonAttachment(): ImageButton
    fun getImageButtonCamera(): ImageButton
    fun getImageButtonEmoji(): ImageButton
    fun getTextCancelAudio(): TextView
    fun hideImageButtonCamera()
    fun showImageButtonCamera()
    fun openQuote(messageAndAttachment: MessageAndAttachment)
    fun getWebIdQuote(): String
    fun closeQuote()
    fun resetImage()
    fun getQuote(): MessageAndAttachment?
    fun changeViewSwitcherToCancel()
    fun changeViewSwitcherToSlideToCancel()
    fun changeViewSwitcherToInputPanel()
    fun setRecordingTime(time: Long)
    fun clearTextEditText()
}