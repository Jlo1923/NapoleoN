package com.naposystems.pepito.ui.custom.inputPanel

import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageButton
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.ui.custom.FabSend

interface IContractInputPanel {
    fun setEditTextWatcher(textWatcher: TextWatcher)
    fun getFloatingActionButton(): FabSend
    fun morphFloatingActionButtonIcon()
    fun getIsShowingMic()
    fun getEditTex(): EditText
    fun getImageButtonAttachment(): ImageButton
    fun getImageButtonCamera(): ImageButton
    fun getImageButtonEmoji(): ImageButton
    fun hideImageButtonCamera()
    fun showImageButtonCamera()
    fun openQuote(messageAndAttachment: MessageAndAttachment)
}