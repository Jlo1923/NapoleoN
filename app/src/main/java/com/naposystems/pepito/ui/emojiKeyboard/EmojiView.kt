package com.naposystems.pepito.ui.emojiKeyboard

import android.content.Context
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.EmojiKeyboardBinding

class EmojiView constructor(context: Context) : ConstraintLayout(context) {

    private lateinit var binding: EmojiKeyboardBinding

    init {
        val infService = Context.LAYOUT_INFLATER_SERVICE
        val layoutInflater =
            getContext().getSystemService(infService) as LayoutInflater

        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.emoji_keyboard,
            this,
            true
        )
    }
}