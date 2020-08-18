package com.naposystems.napoleonchat.ui.napoleonKeyboardEmoji.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.naposystems.napoleonchat.model.emojiKeyboard.EmojiCategory
import com.naposystems.napoleonchat.ui.napoleonKeyboardEmojiPage.NapoleonKeyboardEmojiPageFragment

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