package com.naposystems.napoleonchat.ui.custom.microphoneRecorderView

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.*
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.view.ViewCompat
import com.naposystems.napoleonchat.R
import timber.log.Timber

class MicrophoneRecorderView constructor(context: Context, attrs: AttributeSet) :
    FrameLayout(context, attrs), View.OnTouchListener {

    private var floatingRecordButton: FloatingRecordButton? = null
    private var recordButton: ImageView? = null

    override fun onFinishInflate() {
        super.onFinishInflate()

        floatingRecordButton = FloatingRecordButton(
            context,
            findViewById(R.id.quick_audio_fab)
        )

        recordButton = this.findViewById(R.id.imageButton_audio_toggle)
        recordButton?.setOnTouchListener(this)
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        Timber.i("onTouch")
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                Timber.i("onTouch DOWN")
                recordButton?.visibility = View.INVISIBLE
                floatingRecordButton?.display(event.x, event.y)
            }
            MotionEvent.ACTION_MOVE -> {
                Timber.i("onTouch DOWN")
                floatingRecordButton?.moveTo(event.x, event.y)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                hideUi()
            }
        }

        return false
    }

    private fun hideUi() {
        floatingRecordButton!!.hide()
        recordButton?.visibility = View.VISIBLE
    }

    private class FloatingRecordButton internal constructor(
        context: Context,
        private val recordButtonFab: ImageView
    ) {
        private var startPositionX = 0f
        private var startPositionY = 0f
        private var lastOffsetX = 0f
        private var lastOffsetY = 0f
        fun display(x: Float, y: Float) {
            startPositionX = x
            startPositionY = y
            recordButtonFab.visibility = View.VISIBLE

            val animation = AnimationSet(true)
            animation.addAnimation(
                TranslateAnimation(
                    Animation.ABSOLUTE, 0f,
                    Animation.ABSOLUTE, 0f,
                    Animation.ABSOLUTE, 0f,
                    Animation.ABSOLUTE, 0f
                )
            )
            animation.addAnimation(
                ScaleAnimation(
                    .5f, 1f, .5f, 1f,
                    Animation.RELATIVE_TO_SELF, .5f,
                    Animation.RELATIVE_TO_SELF, .5f
                )
            )
            animation.duration = 200L
            animation.interpolator = OvershootInterpolator()
            recordButtonFab.startAnimation(animation)
        }

        fun moveTo(x: Float, y: Float) {
            lastOffsetX = getXOffset(x)
            lastOffsetY = getYOffset(y)
            if (Math.abs(lastOffsetX) > Math.abs(lastOffsetY)) {
                lastOffsetY = 0f
            } else {
                lastOffsetX = 0f
            }
            recordButtonFab.translationX = lastOffsetX
            recordButtonFab.translationY = lastOffsetY
        }

        fun hide() {
            recordButtonFab.translationX = 0f
            recordButtonFab.translationY = 0f
            if (recordButtonFab.visibility != View.VISIBLE) return
            val animation = AnimationSet(false)
            val scaleAnimation: Animation = ScaleAnimation(
                1f, 0.5f, 1f, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
            )
            val translateAnimation: Animation = TranslateAnimation(
                Animation.ABSOLUTE, lastOffsetX,
                Animation.ABSOLUTE, 0f,
                Animation.ABSOLUTE, lastOffsetY,
                Animation.ABSOLUTE, 0f
            )
            scaleAnimation.interpolator = AnticipateOvershootInterpolator(1.5f)
            translateAnimation.interpolator = DecelerateInterpolator()
            animation.addAnimation(scaleAnimation)
            animation.addAnimation(translateAnimation)
            animation.duration = 200L
            animation.interpolator = AnticipateOvershootInterpolator(1.5f)
            recordButtonFab.visibility = View.GONE
            recordButtonFab.clearAnimation()
            recordButtonFab.startAnimation(animation)
        }

        private fun getXOffset(x: Float): Float {
            return if (ViewCompat.getLayoutDirection(recordButtonFab) == ViewCompat.LAYOUT_DIRECTION_LTR) -Math.max(
                0f,
                startPositionX - x
            ) else Math.max(0f, x - startPositionX)
        }

        private fun getYOffset(y: Float): Float {
            return Math.min(0f, y - startPositionY)
        }

        init {
            /*recordButtonFab.background.setColorFilter(
                context.resources
                    .getColor(android.R.color.holo_red_dark),
                PorterDuff.Mode.SRC_IN
            )*/
        }
    }
}