package com.naposystems.pepito.ui.napoleonKeyboardEmojiPage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.emoji.text.EmojiCompat
import androidx.fragment.app.Fragment
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.NapoleonKeyboardEmojiPageFragmentBinding
import com.naposystems.pepito.model.emojiKeyboard.Emoji
import com.naposystems.pepito.model.emojiKeyboard.EmojiCategory
import com.naposystems.pepito.reactive.RxBus
import com.naposystems.pepito.reactive.RxEvent
import com.naposystems.pepito.ui.napoleonKeyboardEmojiPage.adapter.NapoleonKeyboardEmojiPageAdapter
import com.naposystems.pepito.utility.adapters.showToast

class NapoleonKeyboardEmojiPageFragment : Fragment() {

    companion object {

        const val CATEGORY_KEY: String = "category"
        const val LISTENER_KEY: String = "listener"

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
                    /*if (bundle.containsKey(LISTENER_KEY)) {
                        val listenerValue = bundle.getSerializable(LISTENER_KEY)
                        listenerValue?.let { listener ->
                            setupAdapter(listener as NapoleonKeyboardEmojiPageAdapter.OnNapoleonKeyboardEmojiPageAdapterListener)
                        }
                    }*/
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
                    RxBus.publish(RxEvent.EmojiSelected(emoji))
                }
            })
            adapter.submitList(it.emojiList)

            binding.recyclerViewEmojis.adapter = adapter
        }
    }

}
