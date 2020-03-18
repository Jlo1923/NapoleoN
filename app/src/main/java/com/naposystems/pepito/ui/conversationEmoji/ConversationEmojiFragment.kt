package com.naposystems.pepito.ui.conversationEmoji

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.ConversationEmojiFragmentBinding
import com.naposystems.pepito.ui.emojiKeyboard.EmojiKeyboard
import com.vanniktech.emoji.*
import com.vanniktech.emoji.emoji.Emoji
import com.vanniktech.emoji.listeners.OnEmojiClickListener
import com.vanniktech.emoji.listeners.OnEmojiLongClickListener

class ConversationEmojiFragment : Fragment() {

    companion object {
        fun newInstance() = ConversationEmojiFragment()
    }

    private lateinit var emojiEditTex: EmojiEditText

    private val viewModel: ConversationEmojiViewModel by viewModels()
    private lateinit var binding: ConversationEmojiFragmentBinding

    private val emojiPopup: EmojiPopup by lazy {
        EmojiPopup.Builder.fromRootView(binding.container)
            .build(emojiEditTex)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.conversation_emoji_fragment, container, false
        )

        val emojiKeyboard = EmojiKeyboard(binding.container, emojiEditTex)
        emojiKeyboard.toggle()

        return binding.root
    }

    fun setEmojiEditTex(emojiEditText: EmojiEditText) {
        this.emojiEditTex = emojiEditText
    }

}
