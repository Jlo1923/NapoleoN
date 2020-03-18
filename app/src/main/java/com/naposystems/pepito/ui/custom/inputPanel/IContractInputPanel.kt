package com.naposystems.pepito.ui.custom.inputPanel

import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageButton
import androidx.emoji.widget.EmojiAppCompatEditText
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.vanniktech.emoji.EmojiEditText

interface IContractInputPanel {

    fun setEditTextWatcher(textWatcher: TextWatcher)

    fun morphFloatingActionButtonIcon()

    fun getEditTex(): EmojiEditText

    fun getFloatingActionButton(): FloatingActionButton

    fun getImageButtonEmoji(): ImageButton

    fun getImageButtonAttachment(): ImageButton

    fun getImageButtonCamera(): ImageButton

    fun hideImageButtonCamera()

    fun showImageButtonCamera()
}