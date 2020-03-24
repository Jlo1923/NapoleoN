package com.naposystems.pepito.ui.emojiKeyboard.view

import android.content.Context
import android.view.KeyEvent
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.emoji.widget.EmojiAppCompatEditText
import com.google.android.material.tabs.TabLayoutMediator
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.EmojiKeyboardBinding
import com.naposystems.pepito.ui.emojiKeyboard.adapter.EmojiKeyboardViewPagerAdapter
import com.naposystems.pepito.ui.emojiKeyboardPage.adapter.EmojiKeyboardPageAdapter
import com.naposystems.pepito.ui.mainActivity.MainActivity
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.emojiManager.EmojiManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class EmojiView constructor(context: Context) : ConstraintLayout(context), IContractEmojiView {

    private var binding: EmojiKeyboardBinding
    private lateinit var emojiEditText: EmojiAppCompatEditText

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

        binding.imageViewBackspace.setOnClickListener {
            if (::emojiEditText.isInitialized) {
                val event = KeyEvent(
                    0,
                    0,
                    0,
                    KeyEvent.KEYCODE_DEL,
                    0,
                    0,
                    0,
                    0,
                    KeyEvent.KEYCODE_ENDCALL
                )
                emojiEditText.dispatchKeyEvent(event)
            }
        }
    }

    fun setListener(listener: EmojiKeyboardPageAdapter.EmojiKeyboardPageListener) {
        GlobalScope.launch {
            setupAdapter(listener)
        }
    }

    private fun setupAdapter(listener: EmojiKeyboardPageAdapter.EmojiKeyboardPageListener) {
        val categories = EmojiManager.instance.getEmojiCategories()
        val adapter = EmojiKeyboardViewPagerAdapter(context as MainActivity, listener)
        adapter.addCategories(categories)
        binding.viewPagerEmojiKeyboard.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPagerEmojiKeyboard) { tab, position ->
            val icon = when (categories[position].id) {
                Constants.EmojiCategory.SMILES_AND_PEOPLE.category -> R.drawable.emoji_category_smileysandpeople
                Constants.EmojiCategory.ANIMALS_AND_NATURE.category -> R.drawable.emoji_category_animalsandnature
                Constants.EmojiCategory.FOOD_AND_DRINK.category -> R.drawable.emoji_category_foodanddrink
                Constants.EmojiCategory.ACTIVITY.category -> R.drawable.emoji_category_activities
                Constants.EmojiCategory.TRAVEL_AND_PLACES.category -> R.drawable.emoji_category_travelandplaces
                Constants.EmojiCategory.OBJECTS.category -> R.drawable.emoji_category_objects
                Constants.EmojiCategory.SYMBOLS.category -> R.drawable.emoji_category_symbols
                Constants.EmojiCategory.FLAGS.category -> R.drawable.emoji_category_flags
                else -> R.drawable.ic_notification_icon
            }
            tab.setIcon(icon)
        }.attach()
    }

    //region Implementation IContractEmojiView

    override fun setEditText(emojiAppCompatEditText: EmojiAppCompatEditText) {
        this.emojiEditText = emojiAppCompatEditText
    }
    //endregion
}