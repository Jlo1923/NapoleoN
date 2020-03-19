package com.naposystems.pepito.ui.emojiKeyboard

import android.content.Context
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.google.android.material.tabs.TabLayoutMediator
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.EmojiKeyboardBinding
import com.naposystems.pepito.model.emojiKeyboard.EmojiCategory
import com.naposystems.pepito.ui.emojiKeyboard.adapter.EmojiKeyboardViewPagerAdapter
import com.naposystems.pepito.ui.mainActivity.MainActivity
import com.naposystems.pepito.utility.emojiManager.EmojiManager
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class EmojiView constructor(context: Context) : ConstraintLayout(context) {

    private var binding: EmojiKeyboardBinding

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
    }

    fun setupAdapter() {
//        val emojiCategories = getEmojiCategories()

        val adapter = EmojiKeyboardViewPagerAdapter(context as MainActivity)
        adapter.addCategories(EmojiManager.instance.getEmojiCategories())
        binding.viewPagerEmojiKeyboard.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPagerEmojiKeyboard) { tab, position ->
            tab.setIcon(R.drawable.ic_camera_primary)
        }.attach()
    }

    private suspend fun getEmojiCategories(): List<EmojiCategory> {
        var emojiCategories = listOf<EmojiCategory>()

        withContext(Dispatchers.IO) {
            val emojiJson = context.resources
                .openRawResource(R.raw.emojis)
                .bufferedReader()
                .use { it.readLine() }

            val moshi = Moshi.Builder().build()

            val listType = Types.newParameterizedType(List::class.java, EmojiCategory::class.java)
            val adapter: JsonAdapter<List<EmojiCategory>> = moshi.adapter(listType)

            adapter.fromJson(emojiJson)?.let {
                emojiCategories = it
            }
        }

        return emojiCategories
    }
}