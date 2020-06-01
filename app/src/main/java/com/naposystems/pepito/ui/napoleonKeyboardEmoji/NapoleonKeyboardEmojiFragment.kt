package com.naposystems.pepito.ui.napoleonKeyboardEmoji

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.NapoleonKeyboardEmojiFragmentBinding
import com.naposystems.pepito.ui.mainActivity.MainActivity
import com.naposystems.pepito.ui.napoleonKeyboardEmoji.adapter.NapoleonKeyboardEmojiViewPagerAdapter
import com.naposystems.pepito.ui.napoleonKeyboardEmojiPage.adapter.NapoleonKeyboardEmojiPageAdapter
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.emojiManager.EmojiManager

class NapoleonKeyboardEmojiFragment : Fragment() {

    companion object {
        fun newInstance() = NapoleonKeyboardEmojiFragment()
    }

    private lateinit var binding: NapoleonKeyboardEmojiFragmentBinding
    private var mContext: Context? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            layoutInflater, R.layout.napoleon_keyboard_emoji_fragment, container, false
        )

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        this.mContext?.let {
            setupAdapter(it)
        }
    }

    fun setListener(context: Context) {
        this.mContext = context
    }

    private fun setupAdapter(
        context: Context
    ) {
        val categories = EmojiManager.instance.getEmojiCategories()
        val adapter = NapoleonKeyboardEmojiViewPagerAdapter(context as MainActivity)
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

}
