package com.naposystems.napoleonchat.ui.napoleonKeyboard

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
import androidx.emoji.widget.EmojiAppCompatEditText
import com.naposystems.napoleonchat.ui.mainActivity.MainActivity
import com.naposystems.napoleonchat.ui.napoleonKeyboard.view.NapoleonKeyboardView
import com.naposystems.napoleonchat.ui.napoleonKeyboard.view.NapoleonKeyboardView.Companion.GIF_PAGE
import com.naposystems.napoleonchat.ui.napoleonKeyboardGif.NapoleonKeyboardGifFragment
import com.naposystems.napoleonchat.ui.napoleonKeyboardSticker.NapoleonKeyboardStickerFragment
import com.naposystems.napoleonchat.utility.Utils
import com.naposystems.napoleonchat.utility.emojiManager.EmojiResultReceiver
import timber.log.Timber
import java.io.Serializable

class NapoleonKeyboard constructor(
    private val rootView: View,
    private val editText: EmojiAppCompatEditText,
    private val inputTextMainListener: InputTextMainListener
) : IContractNapoleonKeyboard, EmojiResultReceiver.Listener,
    NapoleonKeyboardGifFragment.NapoleonKeyboardGifListener,
    NapoleonKeyboardView.NapoleonKeyboardViewListener,
    NapoleonKeyboardStickerFragment.NapoleonKeyboardStickerListener, Serializable {

    companion object {
        const val MIN_KEYBOARD_HEIGHT: Int = 0
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
    private var isOpenEmojiWindow = false

    private val emojiResultReceiver = EmojiResultReceiver(Handler(Looper.getMainLooper()))

    init {
        try {

            popupWindowEmoji.setOnDismissListener {

                isOpenEmojiWindow = false
                inputTextMainListener.isShowInputTextMain(true)
                inputTextMainListener.updateIconEmoji(true)
                //change current item bottom bar to emoji
                (popupWindowEmoji.contentView as NapoleonKeyboardView).changeCurrentItemToEmoji()
            }
            popupWindowEmoji.apply {
//                isFocusable = true
                contentView = NapoleonKeyboardView(mainActivity)
                (contentView as NapoleonKeyboardView).apply {
                    setListeners(this@NapoleonKeyboard, this@NapoleonKeyboard)
                    setEditText(editText)
                    setListener(this@NapoleonKeyboard)
                }

                inputMethodMode = PopupWindow.INPUT_METHOD_NOT_NEEDED
                isOutsideTouchable = false
                setBackgroundDrawable(null)
            }

            start()
        } catch (e: Exception) {
            Timber.e(e)
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

        isKeyboardOpen = true

        if (isPendingOpen) {
            showAtBottom()
        }
    }

    fun updateKeyboardStateClosed() {
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
                            isKeyboardOpen = false
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
    override fun toggle(keyboardHeight: Int) {
        validateEmoji()

        if (!popupWindowEmoji.isShowing) {
            editText.requestFocus()

            if (isKeyboardOpen) {
                showAtBottom()
            } else {
                isPendingOpen = true
                val inputMethodManager = mainActivity
                    .getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

                inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
            }


        } else {
            dismiss()
        }
    }

    private fun validateEmoji() {
        if (isOpenEmojiWindow) {
            inputTextMainListener.updateIconEmoji(true)
        } else {
            inputTextMainListener.updateIconEmoji(false)
        }
        isOpenEmojiWindow = !isOpenEmojiWindow
    }

    override fun isShowing() = popupWindowEmoji.isShowing

    override fun handleBackButton() {
        when {
            isShowing() && !isShowingGifPageBigger -> {
                normalizePopupHeight()
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

    override fun dispose() {
        popupWindowEmoji.dismiss()
        emojiResultReceiver.setListener(null)
        mainActivity.resetLayoutHeight()
        mainActivity.window.decorView.setOnApplyWindowInsetsListener(null)
    }

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

    override fun showInputTextMain(value: Boolean) {
        inputTextMainListener.isShowInputTextMain(value)
    }
//endregion

    /** [NapoleonKeyboardStickerFragment.NapoleonKeyboardStickerListener] */
//region Implementation NapoleonKeyboardStickerFragment.NapoleonKeyboardStickerListener
    override fun onStickerSelected() {
        dismiss()
    }
//endregion

    interface InputTextMainListener {
        fun isShowInputTextMain(value: Boolean)
        fun updateIconEmoji(showEmoji: Boolean)
    }
}