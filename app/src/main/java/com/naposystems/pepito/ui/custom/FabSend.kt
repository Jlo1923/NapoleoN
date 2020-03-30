package com.naposystems.pepito.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import androidx.core.content.ContextCompat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.naposystems.pepito.R
import timber.log.Timber

class FabSend : FloatingActionButton {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        this.attrs = attrs
    }

    private var micToSend: AnimatedVectorDrawableCompat? = null
    private var sendToMic: AnimatedVectorDrawableCompat? = null
    private var showingMic: Boolean = true
    private var showOnlySendIcon: Boolean = false
    private var attrs: AttributeSet? = null

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.FabIconSend,
            0, 0
        ).apply {
            try {

                showOnlySendIcon = getBoolean(R.styleable.FabIconSend_showOnlySendIcon, false)

                micToSend = AnimatedVectorDrawableCompat.create(context, R.drawable.anim_mic_send)
                sendToMic = AnimatedVectorDrawableCompat.create(context, R.drawable.anim_send_mic)

                setCustomImageDrawable()

                val valueColorFab = TypedValue()
                context.theme.resolveAttribute(R.attr.attrFabIconColor, valueColorFab, true)

                customSize = resources.getDimension(R.dimen.conversation_fab_size).toInt()
                setColorFilter(
                    ContextCompat.getColor(context, valueColorFab.resourceId),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )

                val value = TypedValue()
                context.theme.resolveAttribute(R.attr.attrColorButtonTint, value, true)

                backgroundTintList = ContextCompat.getColorStateList(context, value.resourceId)
            } finally {
                recycle()
            }
        }
    }

    private fun setCustomImageDrawable() {
        if (showOnlySendIcon) {
            setImageDrawable(sendToMic)
        } else {
            setImageDrawable(micToSend)
        }
    }

    fun morph() {
        if (!showOnlySendIcon) {
            val drawable = if (showingMic) micToSend else sendToMic
            setImageDrawable(drawable)
            drawable?.start()
            showingMic = !showingMic
        }
    }

    fun isShowingMic() = showingMic

    fun setShowOnlySendIcon(showOnlySendIcon: Boolean) {
        this.showOnlySendIcon = showOnlySendIcon
        setCustomImageDrawable()
        invalidate()
        requestLayout()
    }
}