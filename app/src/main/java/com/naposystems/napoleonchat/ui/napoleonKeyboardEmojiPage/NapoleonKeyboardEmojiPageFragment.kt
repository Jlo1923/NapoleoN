package com.naposystems.napoleonchat.ui.napoleonKeyboardEmojiPage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.NapoleonKeyboardEmojiPageFragmentBinding
import com.naposystems.napoleonchat.model.emojiKeyboard.Emoji
import com.naposystems.napoleonchat.model.emojiKeyboard.EmojiCategory
import com.naposystems.napoleonchat.ui.napoleonKeyboardEmojiPage.adapter.NapoleonKeyboardEmojiPageAdapter
import com.naposystems.napoleonchat.utility.sharedViewModels.conversation.ConversationShareViewModel

class NapoleonKeyboardEmojiPageFragment : Fragment() {

    companion object {

        const val CATEGORY_KEY: String = "category"

        fun newInstance(
            category: EmojiCategory
        ) = NapoleonKeyboardEmojiPageFragment().apply {
            arguments = Bundle().apply {
                putSerializable(CATEGORY_KEY, category)
            }
        }
    }

    private lateinit var binding: NapoleonKeyboardEmojiPageFragmentBinding
    private var emojiCategory: EmojiCategory? = null
    private val shareViewModel: ConversationShareViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            layoutInflater, R.layout.napoleon_keyboard_emoji_page_fragment, container, false
        )

        arguments?.let { bundle ->
            if (bundle.containsKey(CATEGORY_KEY)) {
                val categoryValue = bundle.getSerializable(CATEGORY_KEY)
                categoryValue?.let { category ->
                    emojiCategory = category as EmojiCategory
                    setupAdapter()
                }
            }
        }

        return binding.root
    }

    private fun setupAdapter() {
        emojiCategory?.let {
            val adapter = NapoleonKeyboardEmojiPageAdapter(object :
                NapoleonKeyboardEmojiPageAdapter.OnNapoleonKeyboardEmojiPageAdapterListener {
                override fun onEmojiClick(emoji: Emoji) {
                    shareViewModel.apply {
                        setEmojiSelected(emoji)
                        resetEmojiSelected()
                    }
                }
            })
            adapter.submitList(it.emojiList)

            binding.recyclerViewEmojis.adapter = adapter
        }
    }

}
