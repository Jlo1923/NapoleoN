package com.naposystems.pepito.ui.emojiKeyboard.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.naposystems.pepito.model.emojiKeyboard.EmojiCategory
import com.naposystems.pepito.ui.emojiKeyboardPage.EmojiKeyboardPageFragment

class EmojiKeyboardViewPagerAdapter constructor(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    private val mListCategories = mutableListOf<EmojiCategory>()

    override fun getItemCount() = mListCategories.size

    override fun createFragment(position: Int): Fragment {
        val emojiCategory = mListCategories[position]

        return EmojiKeyboardPageFragment.newInstance(emojiCategory)
    }

    fun addCategories(listEmojiCategory: List<EmojiCategory>) {
        this.mListCategories.clear()
        this.mListCategories.addAll(listEmojiCategory)
    }
}