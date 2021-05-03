package com.naposystems.napoleonchat.ui.napoleonKeyboard.view

import android.content.Context
import android.graphics.PorterDuff
import android.view.KeyEvent
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.emoji.widget.EmojiAppCompatEditText
import androidx.fragment.app.Fragment
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.NapoleonKeyboardViewBinding
import com.naposystems.napoleonchat.ui.mainActivity.MainActivity
import com.naposystems.napoleonchat.ui.napoleonKeyboard.adapter.NapoleonKeyboardViewPagerAdapter
import com.naposystems.napoleonchat.ui.napoleonKeyboardEmoji.NapoleonKeyboardEmojiFragment
import com.naposystems.napoleonchat.ui.napoleonKeyboardGif.NapoleonKeyboardGifFragment
import com.naposystems.napoleonchat.ui.napoleonKeyboardSticker.NapoleonKeyboardStickerFragment
import com.naposystems.napoleonchat.utility.Utils

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
        fun showInputTextMain(value: Boolean)
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
        napoleonKeyboardGifListener: NapoleonKeyboardGifFragment.NapoleonKeyboardGifListener,
        napoleonKeyboardStickerListener: NapoleonKeyboardStickerFragment.NapoleonKeyboardStickerListener
    ) {
        setupAdapter(
            napoleonKeyboardGifListener,
            napoleonKeyboardStickerListener
        )
    }

    private fun setupAdapter(
        napoleonKeyboardGifListener: NapoleonKeyboardGifFragment.NapoleonKeyboardGifListener,
        napoleonKeyboardStickerListener: NapoleonKeyboardStickerFragment.NapoleonKeyboardStickerListener
    ) {
        val fragments = mutableListOf<Fragment>()

        val napoleonKeyboardEmojiFragment = NapoleonKeyboardEmojiFragment.newInstance()
        napoleonKeyboardEmojiFragment.setListener(context)

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

    fun changeCurrentItemToEmoji() {
        imageViewOptionClickListener(binding.imageViewEmoji, EMOJI_PAGE)
    }

    private fun imageViewOptionClickListener(imageView: ImageView, pageSelected: Int) {
        validateInputTextMain(pageSelected)
        if (actualPageSelected != pageSelected) {

            mListener?.onPageChange(pageSelected)

            when (actualPageSelected) {
                EMOJI_PAGE -> changeTintToActionBarItemBackground(binding.imageViewEmoji)

                NAPOLEON_STICKER_PAGE ->
                    changeTintToActionBarItemBackground(binding.imageViewSticker)
                GIF_PAGE ->
                    changeTintToActionBarItemBackground(binding.imageViewGif)

            }

            binding.viewPagerEmojiKeyboard.setCurrentItem(pageSelected, true)
            changeTintToColorPrimary(imageView)

            actualPageSelected = pageSelected
        }
    }

    private fun validateInputTextMain(pageSelected: Int) {
        when (pageSelected) {
            GIF_PAGE ->
                mListener?.showInputTextMain(false)
            else ->
                mListener?.showInputTextMain(true)
        }
    }

    private fun changeTintToColorPrimary(imageView: ImageView) {
        val imageColor = Utils.convertAttrToColorResource(context, R.attr.colorPrimary)

        imageView.setColorFilter(imageColor, PorterDuff.Mode.SRC_IN)
    }

    private fun changeTintToActionBarItemBackground(imageView: ImageView) {
        val imageColor =
            Utils.convertAttrToColorResource(context, R.attr.attrActionBarItemBackground)

        imageView.setColorFilter(imageColor, PorterDuff.Mode.SRC_IN)
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