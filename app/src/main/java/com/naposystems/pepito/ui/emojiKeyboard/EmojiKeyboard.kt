package com.naposystems.pepito.ui.emojiKeyboard

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
import androidx.appcompat.app.AppCompatActivity
import androidx.emoji.widget.EmojiAppCompatEditText
import com.naposystems.pepito.ui.emojiKeyboard.view.EmojiView
import com.naposystems.pepito.ui.emojiKeyboardPage.adapter.EmojiKeyboardPageAdapter
import com.naposystems.pepito.utility.Utils
import com.naposystems.pepito.utility.emojiManager.EmojiResultReceiver

class EmojiKeyboard constructor(
    private val rootView: View,
    private val editText: EmojiAppCompatEditText,
    private val listener: EmojiKeyboardPageAdapter.EmojiKeyboardPageListener
) : IContractEmojiKeyboard, EmojiResultReceiver.Listener {

    companion object {
        const val MIN_KEYBOARD_HEIGHT: Int = 50
    }

    private val context: AppCompatActivity by lazy {
        rootView.context as AppCompatActivity
    }

    private val popupWindowEmoji: PopupWindow by lazy {
        PopupWindow(context)
    }

    private var isPendingOpen = false
    private var isKeyboardOpen = false
    private var popupWindowHeight: Int = 0

    private val onAttachStateChangeListener = object : View.OnAttachStateChangeListener {
        override fun onViewDetachedFromWindow(v: View?) {
            start()
        }

        override fun onViewAttachedToWindow(v: View?) {
            stop()

            rootView.removeOnAttachStateChangeListener(this)
        }
    }

    private val emojiResultReceiver = EmojiResultReceiver(Handler(Looper.getMainLooper()))

    init {
        popupWindowEmoji.apply {
            contentView = EmojiView(context)
            (contentView as EmojiView).apply {
                setListener(listener)
                setEditText(editText)
            }

            inputMethodMode = PopupWindow.INPUT_METHOD_NOT_NEEDED
            setBackgroundDrawable(null)
        }

        rootView.addOnAttachStateChangeListener(onAttachStateChangeListener)
    }

    private fun updateKeyboardStateOpened(keyboardHeight: Int) {
        if (popupWindowHeight > 0 && popupWindowEmoji.height != popupWindowHeight) {
            popupWindowEmoji.height = popupWindowHeight
        } else if (popupWindowHeight == 0 && popupWindowEmoji.height != keyboardHeight) {
            popupWindowEmoji.height = keyboardHeight
        }

        val rect = Utils.windowVisibleDisplayFrame(context)

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
    }

    private fun start() {
        context.window.decorView
            .setOnApplyWindowInsetsListener(object : View.OnApplyWindowInsetsListener {
                var previousOffset = 0
                override fun onApplyWindowInsets(
                    v: View,
                    insets: WindowInsets
                ): WindowInsets {
                    val offset: Int =
                        if (insets.systemWindowInsetBottom < insets.stableInsetBottom) {
                            insets.systemWindowInsetBottom
                        } else {
                            insets.systemWindowInsetBottom - insets.stableInsetBottom
                        }
                    if (offset != previousOffset || offset == 0) {
                        previousOffset = offset
                        if (offset > Utils.dpToPx(
                                context,
                                MIN_KEYBOARD_HEIGHT.toFloat()
                            )
                        ) {
                            updateKeyboardStateOpened(offset)
                        } else {
                            updateKeyboardStateClosed()
                        }
                    }
                    return context.window.decorView.onApplyWindowInsets(insets)
                }
            })
    }

    private fun stop() {
        dismiss()
        context.window.decorView.setOnApplyWindowInsetsListener(null)
    }

    //region Implementation IContractEmojiKeyboard
    override fun toggle() {
        if (!popupWindowEmoji.isShowing) {
            start()
            editText.isFocusableInTouchMode = true
            editText.requestFocus()
            val inputMethodManager = context
                .getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

            isPendingOpen = true

            emojiResultReceiver.setListener(this)
            inputMethodManager.showSoftInput(
                editText,
                RESULT_UNCHANGED_SHOWN,
                emojiResultReceiver
            )
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
            showAtBottom()
        }
    }
    //endregion
}