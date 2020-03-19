package com.naposystems.pepito.ui.emojiKeyboard

import android.content.Context.INPUT_METHOD_SERVICE
import android.view.Gravity
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import com.naposystems.pepito.utility.Utils

class EmojiKeyboard constructor(
    private val rootView: View,
    private val editText: EditText
) : IContractEmojiKeyboard {

    companion object {
        const val MIN_KEYBOARD_HEIGHT = 50
    }

    private val context: AppCompatActivity by lazy {
        rootView.context as AppCompatActivity
    }

    private val popupWindowEmoji: PopupWindow by lazy {
        PopupWindow(context)
    }

    private var isPendingOpen = false
    private var isKeyboardOpen = false

    private val onGlobalLayoutListener =
        ViewTreeObserver.OnGlobalLayoutListener { updateKeyboardState() }

    init {
        popupWindowEmoji.apply {
            contentView = EmojiView(context)
            (contentView as EmojiView).setupAdapter()
            inputMethodMode = PopupWindow.INPUT_METHOD_NOT_NEEDED
            setBackgroundDrawable(null)
        }

        rootView.viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener)
    }

    private fun updateKeyboardState() {

        val keyboardHeight = Utils.getKeyboardHeight(context)

        if (keyboardHeight > Utils.dpToPx(context, MIN_KEYBOARD_HEIGHT.toFloat())) {
            updateKeyboardStateOpened(keyboardHeight)
        } else {
            updateKeyboardStateClosed()
        }
    }

    private fun updateKeyboardStateOpened(keyboardHeight: Int) {
        if (popupWindowEmoji.height != keyboardHeight) {
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
    }

    //region Implementation IContractEmojiKeyboard
    override fun toggle() {
        if (!popupWindowEmoji.isShowing) {
            editText.isFocusableInTouchMode = true
            editText.requestFocus()
            val inputMethodManager = context
                .getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

            isPendingOpen = true

            inputMethodManager.showSoftInput(
                editText,
                InputMethodManager.RESULT_UNCHANGED_SHOWN
            )
        } else {
            dismiss()
        }
    }

    override fun isShowing() = popupWindowEmoji.isShowing

    //endregion
}