package com.naposystems.pepito.ui.napoleonKeyboard.view

import android.content.Context
import android.graphics.PorterDuff
import android.util.TypedValue
import android.view.KeyEvent
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.emoji.widget.EmojiAppCompatEditText
import androidx.fragment.app.Fragment
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.NapoleonKeyboardViewBinding
import com.naposystems.pepito.ui.mainActivity.MainActivity
import com.naposystems.pepito.ui.napoleonKeyboard.adapter.NapoleonKeyboardViewPagerAdapter
import com.naposystems.pepito.ui.napoleonKeyboardEmoji.NapoleonKeyboardEmojiFragment
import com.naposystems.pepito.ui.napoleonKeyboardEmojiPage.adapter.NapoleonKeyboardEmojiPageAdapter
import com.naposystems.pepito.ui.napoleonKeyboardGif.NapoleonKeyboardGifFragment
import com.naposystems.pepito.ui.napoleonKeyboardSticker.NapoleonKeyboardStickerFragment

class NapoleonKeyboardView constructor(context: Context) : ConstraintLayout(context),
    IContractNapoleonKeyboardView {

    companion object {
        const val EMOJI_PAGE = 0
        const val NAPOLEON_STICKER_PAGE = 1
        const val GIF_PAGE = 2
    }

    private var binding: NapoleonKeyboardViewBinding
    private lateinit var emojiEditText: EmojiAppCompatEditText
    private var actualPageSelected = EMOJI_PAGE
    private var mListener: NapoleonKeyboardViewListener? = null

    interface NapoleonKeyboardViewListener {
        fun onPageChange(page: Int)
    }

    init {
        val infService = Context.LAYOUT_INFLATER_SERVICE
        val layoutInflater =
            getContext().getSystemService(infService) as LayoutInflater

        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.napoleon_keyboard_view,
            this,
            true
        )

        binding.viewPagerEmojiKeyboard.isUserInputEnabled = false

        bottomNavigationListener()

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

    fun setListeners(
        emojiPageAdapterListener: NapoleonKeyboardEmojiPageAdapter.OnNapoleonKeyboardEmojiPageAdapterListener,
        napoleonKeyboardGifListener: NapoleonKeyboardGifFragment.NapoleonKeyboardGifListener,
        napoleonKeyboardStickerListener: NapoleonKeyboardStickerFragment.NapoleonKeyboardStickerListener
    ) {
        setupAdapter(emojiPageAdapterListener, napoleonKeyboardGifListener, napoleonKeyboardStickerListener)
    }

    private fun setupAdapter(
        emojiPageAdapterListener: NapoleonKeyboardEmojiPageAdapter.OnNapoleonKeyboardEmojiPageAdapterListener,
        napoleonKeyboardGifListener: NapoleonKeyboardGifFragment.NapoleonKeyboardGifListener,
        napoleonKeyboardStickerListener: NapoleonKeyboardStickerFragment.NapoleonKeyboardStickerListener
    ) {
        val fragments = mutableListOf<Fragment>()

        val napoleonKeyboardEmojiFragment = NapoleonKeyboardEmojiFragment.newInstance()
        napoleonKeyboardEmojiFragment.setListener(context, emojiPageAdapterListener)

        val napoleonKeyboardGifFragment = NapoleonKeyboardGifFragment.newInstance()
        napoleonKeyboardGifFragment.setListener(napoleonKeyboardGifListener)

        val napoleonKeyboardStickerFragment = NapoleonKeyboardStickerFragment.newInstance()
        napoleonKeyboardStickerFragment.setListener(napoleonKeyboardStickerListener)

        fragments.add(napoleonKeyboardEmojiFragment)
        fragments.add(napoleonKeyboardStickerFragment)
        fragments.add(napoleonKeyboardGifFragment)

        val adapter = NapoleonKeyboardViewPagerAdapter(context as MainActivity)
        adapter.addFragments(fragments)

        binding.viewPagerEmojiKeyboard.adapter = adapter
    }

    private fun bottomNavigationListener() {

        binding.imageViewEmoji.setOnClickListener {
            imageViewOptionClickListener(binding.imageViewEmoji, EMOJI_PAGE)
        }

        binding.imageViewSticker.setOnClickListener {
            imageViewOptionClickListener(binding.imageViewSticker, NAPOLEON_STICKER_PAGE)
        }

        binding.imageViewGif.setOnClickListener {
            imageViewOptionClickListener(binding.imageViewGif, GIF_PAGE)
        }
    }

    private fun imageViewOptionClickListener(imageView: ImageView, pageSelected: Int) {
        if (actualPageSelected != pageSelected) {

            mListener?.onPageChange(pageSelected)

            when (actualPageSelected) {
                EMOJI_PAGE -> changeTintToActionBarItemBackground(binding.imageViewEmoji)
                NAPOLEON_STICKER_PAGE -> changeTintToActionBarItemBackground(binding.imageViewSticker)
                GIF_PAGE -> changeTintToActionBarItemBackground(binding.imageViewGif)
            }

            binding.viewPagerEmojiKeyboard.setCurrentItem(pageSelected, true)
            changeTintToColorPrimary(imageView)

            actualPageSelected = pageSelected
        }
    }

    private fun changeTintToColorPrimary(imageView: ImageView) {

        val outValueColorPrimary = TypedValue()
        context.theme.resolveAttribute(
            R.attr.colorPrimary,
            outValueColorPrimary,
            true
        )

        imageView.setColorFilter(
            ContextCompat.getColor(
                context,
                outValueColorPrimary.resourceId
            ),
            PorterDuff.Mode.SRC_IN
        )
    }

    private fun changeTintToActionBarItemBackground(imageView: ImageView) {
        val outValueActionBarItemBackgroundTint = TypedValue()
        context.theme.resolveAttribute(
            R.attr.attrActionBarItemBackground,
            outValueActionBarItemBackgroundTint,
            true
        )

        imageView.setColorFilter(
            ContextCompat.getColor(
                context,
                outValueActionBarItemBackgroundTint.resourceId
            ),
            PorterDuff.Mode.SRC_IN
        )
    }

    //region Implementation IContractEmojiView

    override fun setEditText(emojiAppCompatEditText: EmojiAppCompatEditText) {
        this.emojiEditText = emojiAppCompatEditText
    }

    override fun setListener(listener: NapoleonKeyboardViewListener) {
        this.mListener = listener
    }

    //endregion
}