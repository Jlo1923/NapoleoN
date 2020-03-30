package com.naposystems.pepito.ui.custom.inputPanel

import android.text.TextWatcher
import android.widget.ImageButton
import androidx.emoji.widget.EmojiAppCompatEditText
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.ui.custom.FabSend

interface IContractInputPanel {
    fun setEditTextWatcher(textWatcher: TextWatcher)
    fun getFloatingActionButton(): FabSend
    fun morphFloatingActionButtonIcon()
    fun getEditTex(): EmojiAppCompatEditText
    fun getImageButtonAttachment(): ImageButton
    fun getImageButtonCamera(): ImageButton
    fun getImageButtonEmoji(): ImageButton
    fun hideImageButtonCamera()
    fun showImageButtonCamera()
    fun openQuote(messageAndAttachment: MessageAndAttachment)
    fun getWebIdQuote(): String
    fun closeQuote()
    fun resetImage()
    fun getQuote(): MessageAndAttachment?
}