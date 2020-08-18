package com.naposystems.napoleonchat.ui.napoleonKeyboardSticker.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.naposystems.napoleonchat.model.napoleonEmoji.NapoleonEmojiRemoteConfig
import com.naposystems.napoleonchat.ui.napoleonKeyboardSticker.NapoleonKeyboardStickerFragment
import com.naposystems.napoleonchat.ui.napoleonKeyboardStickerPage.NapoleonKeyboardStickerPageFragment

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