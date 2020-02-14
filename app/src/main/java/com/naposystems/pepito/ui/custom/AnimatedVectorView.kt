package com.naposystems.pepito.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.naposystems.pepito.R
import com.naposystems.pepito.utility.Utils

class AnimatedVectorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    AppCompatImageView(context, attrs, defStyleAttr) {

    private var editToCancelAnim: AnimatedVectorDrawableCompat? = null
    private var cancelToHourglassAnim: AnimatedVectorDrawableCompat? = null
    private var hourglassToCancelAnim: AnimatedVectorDrawableCompat? = null
    private var hourglassToEditAnim: AnimatedVectorDrawableCompat? = null
    private var cancelToEditAnim: AnimatedVectorDrawableCompat? = null

    var hasBeenInitialized = false

    init {
        editToCancelAnim = AnimatedVectorDrawableCompat
            .create(context, R.drawable.anim_edit_cancel)
        cancelToHourglassAnim = AnimatedVectorDrawableCompat
            .create(context, R.drawable.anim_cancel_hourglass)
        hourglassToCancelAnim = AnimatedVectorDrawableCompat
            .create(context, R.drawable.anim_hourglass_cancel)
        hourglassToEditAnim = AnimatedVectorDrawableCompat
            .create(context, R.drawable.anim_hourglass_edit)
        cancelToEditAnim = AnimatedVectorDrawableCompat
            .create(context, R.drawable.anim_cancel_edit)
    }

    fun editToCancel(editText: EditText) {
        morph(editToCancelAnim)
        hasBeenInitialized = true
        enabledEditText(editText)
    }

    private fun enabledEditText(editText: EditText) {
        editText.apply {
            isEnabled = true
            isFocusable = true
            requestFocus()
            setSelection(this.text!!.length)
            Utils.openKeyboard(this)
        }
    }

    private fun disabledEditText(editText: EditText) {
        editText.apply {
            isEnabled = false
            isFocusable = false
        }
    }

    fun cancelToHourglass() {
        morph(cancelToHourglassAnim)
        isEnabled = false
    }

    fun hourglassToCancel() {
        morph(hourglassToCancelAnim)
        isEnabled = true
    }

    fun hourglassToEdit() {
        morph(hourglassToEditAnim)
        isEnabled = true
        hasBeenInitialized = false
    }

    fun cancelToEdit(editText: EditText) {
        morph(cancelToEditAnim)
        hasBeenInitialized = false
        disabledEditText(editText)
    }

    private fun morph(animatedVectorDrawable: AnimatedVectorDrawableCompat?) {
        setImageDrawable(animatedVectorDrawable)
        animatedVectorDrawable?.start()
    }
}