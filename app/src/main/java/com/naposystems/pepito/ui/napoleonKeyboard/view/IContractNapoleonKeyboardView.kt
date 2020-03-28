package com.naposystems.pepito.ui.napoleonKeyboard.view

import androidx.emoji.widget.EmojiAppCompatEditText

interface IContractNapoleonKeyboardView {

    fun setEditText(emojiAppCompatEditText: EmojiAppCompatEditText)

    fun setListener(listener: NapoleonKeyboardView.NapoleonKeyboardViewListener)
}