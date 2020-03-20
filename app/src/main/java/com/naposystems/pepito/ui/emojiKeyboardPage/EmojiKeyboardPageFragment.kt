package com.naposystems.pepito.ui.emojiKeyboardPage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.EmojiKeyboardPageFragmentBinding
import com.naposystems.pepito.model.emojiKeyboard.EmojiCategory
import com.naposystems.pepito.ui.emojiKeyboardPage.adapter.EmojiKeyboardPageAdapter

class EmojiKeyboardPageFragment : Fragment() {

    companion object {

        const val CATEGORY_KEY: String = "category"
        const val LISTENER_KEY: String = "listener"

        fun newInstance(
            category: EmojiCategory,
            listener: EmojiKeyboardPageAdapter.EmojiKeyboardPageListener
        ) = EmojiKeyboardPageFragment().apply {
            arguments = Bundle().apply {
                putSerializable(CATEGORY_KEY, category)
                putSerializable(LISTENER_KEY, listener)
            }
        }
    }

    private val viewModel: EmojiKeyboardPageViewModel by viewModels()
    private lateinit var binding: EmojiKeyboardPageFragmentBinding
    private var emojiCategory: EmojiCategory? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            layoutInflater, R.layout.emoji_keyboard_page_fragment, container, false
        )

        arguments?.let { bundle ->
            if (bundle.containsKey(CATEGORY_KEY)) {
                val categoryValue = bundle.getSerializable(CATEGORY_KEY)
                categoryValue?.let { category ->
                    emojiCategory = category as EmojiCategory
                    if (bundle.containsKey(LISTENER_KEY)) {
                        val listenerValue = bundle.getSerializable(LISTENER_KEY)
                        listenerValue?.let { listener ->
                            setupAdapter(listener as EmojiKeyboardPageAdapter.EmojiKeyboardPageListener)
                        }
                    }
                }
            }
        }

        return binding.root
    }

    private fun setupAdapter(listener: EmojiKeyboardPageAdapter.EmojiKeyboardPageListener) {
        emojiCategory?.let {
            val adapter = EmojiKeyboardPageAdapter(listener)
            adapter.submitList(it.emojiList)

            binding.recyclerViewEmojis.adapter = adapter
        }
    }

}
