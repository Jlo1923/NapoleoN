package com.naposystems.pepito.ui.emojiKeyboard.view

import android.app.Instrumentation
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
                GlobalScope.launch {
                    val instrumentation = Instrumentation()
                    instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DEL)
                }
            }
        }
    }

    fun setListener(listener: EmojiKeyboardPageAdapter.EmojiKeyboardPageListener) {
        GlobalScope.launch {
            setupAdapter(listener)
        }
    }

    private fun setupAdapter(listener: EmojiKeyboardPageAdapter.EmojiKeyboardPageListener) {
        val adapter = EmojiKeyboardViewPagerAdapter(context as MainActivity, listener)
        adapter.addCategories(EmojiManager.instance.getEmojiCategories())
        binding.viewPagerEmojiKeyboard.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPagerEmojiKeyboard) { tab, position ->
            tab.setIcon(R.drawable.ic_camera_primary)
        }.attach()
    }

    //region Implementation IContractEmojiView

    override fun setEditText(emojiAppCompatEditText: EmojiAppCompatEditText) {
        this.emojiEditText = emojiAppCompatEditText
    }
    //endregion
}