package com.naposystems.napoleonchat.ui.custom.animatedTwoVectorView

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.naposystems.napoleonchat.R

class AnimatedTwoVectorView(context: Context, attrs: AttributeSet) :
    AppCompatImageView(context, attrs), IContractAnimatedTwoVectorView {

    private var firstAnimation: AnimatedVectorDrawableCompat? = null
    private var secondAnimation: AnimatedVectorDrawableCompat? = null

    var hasBeenInitialized = false

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.AnimatedTwoVectorView,
            0, 0
        ).apply {
            try {

                firstAnimation = AnimatedVectorDrawableCompat.create(
                    context,
                    getResourceId(
                        R.styleable.AnimatedTwoVectorView_firstAnimation,
                        R.drawable.anim_mic_send
                    )
                )

                secondAnimation = AnimatedVectorDrawableCompat.create(
                    context,
                    getResourceId(
                        R.styleable.AnimatedTwoVectorView_secondAnimation,
                        R.drawable.anim_send_mic
                    )
                )

            } finally {
                recycle()
            }
        }
    }

    private fun morph() {
        if (hasBeenInitialized) {
            setImageDrawable(firstAnimation)
            firstAnimation?.start()
        } else {
            setImageDrawable(secondAnimation)
            secondAnimation?.start()
        }
    }

    //region Implementation IContractAnimatedTwoVectorView
    override fun playAnimation() {
        hasBeenInitialized = true
        morph()
    }

    override fun reverseAnimation() {
        hasBeenInitialized = false
        morph()
    }

    override fun hasBeenInitialized() = hasBeenInitialized
    //endregion
}