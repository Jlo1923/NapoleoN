package com.naposystems.pepito.ui.napoleonKeyboardSticker.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.naposystems.pepito.model.napoleonEmoji.NapoleonEmojiRemoteConfig
import com.naposystems.pepito.ui.napoleonKeyboardSticker.NapoleonKeyboardStickerFragment
import com.naposystems.pepito.ui.napoleonKeyboardStickerPage.NapoleonKeyboardStickerPageFragment

class NapoleonKeyboardStickerPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val napoleonEmojiList: List<NapoleonEmojiRemoteConfig>,
    private val listener: NapoleonKeyboardStickerFragment.NapoleonKeyboardStickerListener?
) :
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount() = napoleonEmojiList.size

    override fun createFragment(position: Int): Fragment {
        val fragment = NapoleonKeyboardStickerPageFragment.newInstance(napoleonEmojiList[position])
        fragment.setListener(listener)
        return fragment
    }
}