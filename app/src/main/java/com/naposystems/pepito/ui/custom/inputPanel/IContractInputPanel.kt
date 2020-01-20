package com.naposystems.pepito.ui.custom.inputPanel

import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

interface IContractInputPanel {

    fun setEditTextWatcher(textWatcher: TextWatcher)

    fun morphFloatingActionButtonIcon()

    fun getEditTex(): EditText

    fun getFloatingActionButton(): FloatingActionButton

    fun getImageButtonEmoji(): ImageButton

    fun getImageButtonAttachment(): ImageButton

    fun getImageButtonCamera(): ImageButton

    fun hideImageButtonCamera()

    fun showImageButtonCamera()
}