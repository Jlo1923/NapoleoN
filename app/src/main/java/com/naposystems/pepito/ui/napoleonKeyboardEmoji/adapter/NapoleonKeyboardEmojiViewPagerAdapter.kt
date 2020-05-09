package com.naposystems.pepito.ui.napoleonKeyboardEmoji.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.naposystems.pepito.model.emojiKeyboard.EmojiCategory
import com.naposystems.pepito.ui.napoleonKeyboardEmojiPage.NapoleonKeyboardEmojiPageFragment
import com.naposystems.pepito.ui.napoleonKeyboardEmojiPage.adapter.NapoleonKeyboardEmojiPageAdapter

class NapoleonKeyboardEmojiViewPagerAdapter constructor(
    fragmentActivity: FragmentActivity
) :
    FragmentStateAdapter(fragmentActivity) {

    private val mListCategories = mutableListOf<EmojiCategory>()

    override fun getItemCount() = mListCategories.size

    override fun createFragment(position: Int): Fragment {
        val emojiCategory = mListCategories[position]

        return NapoleonKeyboardEmojiPageFragment.newInstance(emojiCategory)
    }

    fun addCategories(listEmojiCategory: List<EmojiCategory>) {
        this.mListCategories.clear()
        this.mListCategories.addAll(listEmojiCategory)
    }
}