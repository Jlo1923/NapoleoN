package com.naposystems.pepito.ui.napoleonKeyboard

import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.RESULT_UNCHANGED_HIDDEN
import android.view.inputmethod.InputMethodManager.RESULT_UNCHANGED_SHOWN
import android.widget.PopupWindow
import androidx.activity.addCallback
import androidx.emoji.widget.EmojiAppCompatEditText
import com.naposystems.pepito.ui.mainActivity.MainActivity
import com.naposystems.pepito.ui.napoleonKeyboard.view.NapoleonKeyboardView
import com.naposystems.pepito.ui.napoleonKeyboard.view.NapoleonKeyboardView.Companion.GIF_PAGE
import com.naposystems.pepito.ui.napoleonKeyboardEmojiPage.adapter.NapoleonKeyboardEmojiPageAdapter
import com.naposystems.pepito.ui.napoleonKeyboardGif.NapoleonKeyboardGifFragment
import com.naposystems.pepito.ui.napoleonKeyboardSticker.NapoleonKeyboardStickerFragment
import com.naposystems.pepito.utility.Utils
import com.naposystems.pepito.utility.emojiManager.EmojiResultReceiver
import java.io.Serializable


class NapoleonKeyboard constructor(
    private val rootView: View,
    private val editText: EmojiAppCompatEditText,
    private val listener: NapoleonKeyboardEmojiPageAdapter.OnNapoleonKeyboardEmojiPageAdapterListener
) : IContractNapoleonKeyboard, EmojiResultReceiver.Listener,
    NapoleonKeyboardGifFragment.NapoleonKeyboardGifListener,
    NapoleonKeyboardView.NapoleonKeyboardViewListener,
    NapoleonKeyboardStickerFragment.NapoleonKeyboardStickerListener, Serializable {

    companion object {
        const val MIN_KEYBOARD_HEIGHT: Int = 50
    }

    private val mainActivity: MainActivity by lazy {
        rootView.context as MainActivity
    }

    private val popupWindowEmoji: PopupWindow by lazy {
        PopupWindow(mainActivity)
    }

    private var isPendingOpen = false
    private var isKeyboardOpen = false
    private var isShowingKeyboard = false
    private var isShowingGifPageBigger = false
    private var popupWindowHeight: Int = 0
    private var windowInsets: WindowInsets? = null

    private val emojiResultReceiver = EmojiResultReceiver(Handler(Looper.getMainLooper()))

    init {
        popupWindowEmoji.apply {
            isFocusable = true
            contentView = NapoleonKeyboardView(mainActivity)
            (contentView as NapoleonKeyboardView).apply {
                setListeners(listener, this@NapoleonKeyboard, this@NapoleonKeyboard)
                setEditText(editText)
                setListener(this@NapoleonKeyboard)
            }

            inputMethodMode = PopupWindow.INPUT_METHOD_NOT_NEEDED
            setBackgroundDrawable(null)
        }

        mainActivity.onBackPressedDispatcher.addCallback() {
            when {
                isShowing() && !isShowingGifPageBigger -> {
                    dismiss()
                }
                isShowingGifPageBigger -> {
                    normalizePopupHeight()
                }
                else -> {
                    mainActivity.getNavController().navigateUp()
                }
            }

        }
    }


    private fun updateKeyboardStateOpened(keyboardHeight: Int) {
        if (popupWindowHeight > 0 && popupWindowEmoji.height != popupWindowHeight) {
            popupWindowEmoji.height = popupWindowHeight
        } else if (popupWindowHeight == 0 && popupWindowEmoji.height != keyboardHeight) {
            popupWindowEmoji.height = keyboardHeight
        }

        val rect = Utils.windowVisibleDisplayFrame(mainActivity)

        val properWidth = rect.right

        if (popupWindowEmoji.width != properWidth) {
            popupWindowEmoji.width = properWidth
        }

        if (!isKeyboardOpen) {
            isKeyboardOpen = true
        }

        if (isPendingOpen) {
            showAtBottom()
        }
    }

    private fun updateKeyboardStateClosed() {
        isKeyboardOpen = false

        if (isShowing()) {
            dismiss()
        }
    }

    private fun showAtBottom() {
        isPendingOpen = false
        popupWindowEmoji.showAtLocation(rootView, Gravity.BOTTOM, 0, 0)
    }

    private fun dismiss() {
        popupWindowEmoji.dismiss()
        emojiResultReceiver.setListener(null)
        mainActivity.resetLayoutHeight()
        mainActivity.window.decorView.setOnApplyWindowInsetsListener(null)
    }

    private fun start() {
        mainActivity.window.decorView
            .setOnApplyWindowInsetsListener(object : View.OnApplyWindowInsetsListener {
                var previousOffset = 0
                override fun onApplyWindowInsets(
                    v: View,
                    insets: WindowInsets
                ): WindowInsets {
                    windowInsets = insets
                    val offset: Int =
                        if (insets.systemWindowInsetBottom < insets.stableInsetBottom) {
                            insets.systemWindowInsetBottom
                        } else {
                            insets.systemWindowInsetBottom - insets.stableInsetBottom
                        }
                    if (offset != previousOffset || offset == 0) {
                        previousOffset = offset
                        if (offset > Utils.dpToPx(
                                mainActivity,
                                MIN_KEYBOARD_HEIGHT.toFloat()
                            )
                        ) {
                            updateKeyboardStateOpened(
                                offset
                            )
                        } else {
                            if (isShowingKeyboard) {
                                windowInsets?.let {
                                    popupWindowEmoji.isFocusable = false
                                    popupWindowEmoji.inputMethodMode =
                                        PopupWindow.INPUT_METHOD_NOT_NEEDED
                                    popupWindowEmoji.update(
                                        0,
                                        0,
                                        popupWindowEmoji.width,
                                        popupWindowEmoji.height * 2
                                    )
                                    isShowingKeyboard = false
                                    mainActivity.changeLayoutHeight(popupWindowEmoji.height - it.stableInsetTop)
                                    isShowingGifPageBigger = true
                                }
                            } else {
                                updateKeyboardStateClosed()
                            }
                        }
                    }


                    return mainActivity.window.decorView.onApplyWindowInsets(insets)
                }
            })
    }

    private fun normalizePopupHeight() {
        windowInsets?.let {
            popupWindowEmoji.update(
                0,
                0,
                popupWindowEmoji.width,
                popupWindowEmoji.height / 2
            )
            mainActivity.changeLayoutHeight(popupWindowEmoji.height - it.stableInsetTop)
            isShowingGifPageBigger = false
        }
    }

    //region Implementation IContractEmojiKeyboard
    override fun toggle() {
        if (!popupWindowEmoji.isShowing) {
            start()
            editText.requestFocus()
            val inputMethodManager = mainActivity
                .getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

            isPendingOpen = true

            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        } else {
            dismiss()
        }
    }

    override fun isShowing() = popupWindowEmoji.isShowing

    //endregion

    /** [EmojiResultReceiver.Listener] */
    //region Implementation EmojiResultReceiver.Listener
    override fun onReceivedResult(resultCode: Int, data: Bundle?) {
        if (resultCode == RESULT_UNCHANGED_SHOWN || resultCode == RESULT_UNCHANGED_HIDDEN) {
            // Intentionally empty
        }
    }
    //endregion

    /** [NapoleonKeyboardGifFragment.NapoleonKeyboardGifListener] */
    //region Implementation NapoleonKeyboardGifFragment.NapoleonKeyboardGifListener
    override fun onSearchFocused() {
        this.editText.clearFocus()

        popupWindowEmoji.inputMethodMode = PopupWindow.INPUT_METHOD_NEEDED
        popupWindowEmoji.isFocusable = true
        popupWindowEmoji.update()

        windowInsets?.let {
            if (!isShowingGifPageBigger) {
                popupWindowEmoji.update(
                    0,
                    it.systemWindowInsetBottom,
                    popupWindowEmoji.width,
                    popupWindowEmoji.height
                )
            }

            mainActivity.changeLayoutHeight(popupWindowEmoji.height - it.stableInsetTop)

            isShowingKeyboard = true
        }
    }

    /** [NapoleonKeyboardGifFragment.NapoleonKeyboardGifListener] */
    //region Implementation NapoleonKeyboardGifFragment.NapoleonKeyboardGifListener
    override fun onGifSelected() {
        dismiss()
    }
    //endregion

    //endregion

    /** [NapoleonKeyboardView.NapoleonKeyboardViewListener] */
    //region Implementation NapoleonKeyboardView.NapoleonKeyboardViewListener
    override fun onPageChange(page: Int) {
        if (page != GIF_PAGE && isShowingGifPageBigger) {
            normalizePopupHeight()
        }
    }
    //endregion

    /** [NapoleonKeyboardStickerFragment.NapoleonKeyboardStickerListener] */
    //region Implementation NapoleonKeyboardStickerFragment.NapoleonKeyboardStickerListener
    override fun onStickerSelected() {
        dismiss()
    }
    //endregion
}