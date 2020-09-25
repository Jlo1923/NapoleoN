package com.naposystems.napoleonchat.ui.custom.microphoneRecorderView

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.*
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.CustomViewMicrophoneRecorderBinding
import com.naposystems.napoleonchat.utility.Utils
import timber.log.Timber

class MicrophoneRecorderView constructor(context: Context, attrs: AttributeSet) :
    FrameLayout(context, attrs), View.OnTouchListener, IContractMicrophoneRecorder {

    enum class State {
        NOT_RUNNING, RUNNING_HELD, RUNNING_LOCKED
    }

    private var floatingRecordButton: FloatingRecordButton

    private var binding: CustomViewMicrophoneRecorderBinding

    private var mListener: Listener? = null

    var state: State = State.NOT_RUNNING

    private var bandera: Boolean = true
    private val mHandler: Handler = Handler()
    private var runnable: Runnable? = null

    init {
        val infService = Context.LAYOUT_INFLATER_SERVICE
        val layoutInflater =
            getContext().getSystemService(infService) as LayoutInflater

        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.custom_view_microphone_recorder,
            this@MicrophoneRecorderView,
            true
        )

        binding.imageButtonAudioToggle.setOnTouchListener(this)

        floatingRecordButton = FloatingRecordButton(
            context,
            binding.quickAudioFab,
            binding.containerLock
        )
    }

    interface Listener {
        fun checkRecordAudioPermission(successCallback: () -> Unit)
        fun onRecordPressed()
        fun onRecordReleased()
        fun onRecordCanceled()
        fun onRecordLocked()
        fun onRecordMoved(offsetX: Float, absoluteX: Float)
        fun onRecordPermissionRequired()
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        Timber.i("onTouch")
        mListener?.checkRecordAudioPermission {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (bandera) {
                        bandera = false

                        aLaMierda()

                        state = State.RUNNING_HELD
                        binding.imageButtonAudioToggle.isVisible = false
                        floatingRecordButton.display(event.x, event.y)
                        mListener?.onRecordPressed()
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    if (this.state == State.RUNNING_HELD) {
                        floatingRecordButton.moveTo(event.x, event.y)
                        mListener?.onRecordMoved(floatingRecordButton.lastOffsetX, event.rawX)
                        val dimensionPixelSize =
                            resources.getDimensionPixelSize(R.dimen.recording_voice_lock_target)
                        if (floatingRecordButton.lastOffsetY <= dimensionPixelSize) {
                            lockAction()
                        }
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (this.state == State.RUNNING_HELD) {
                        this.state = State.NOT_RUNNING
                        hideUi()
                        mListener?.onRecordReleased()
                    }
                }
            }
        }


        return false
    }

    private fun aLaMierda() {
        runnable = Runnable {
            bandera = true
        }

        handler.postDelayed(runnable, 400)
    }

    private fun lockAction() {
        if (this.state == State.RUNNING_HELD) {
            this.state = State.RUNNING_LOCKED
            hideUi()
            mListener?.onRecordLocked()
        }
    }

    private fun hideUi() {
        floatingRecordButton.hide()
        binding.imageButtonAudioToggle.isVisible = true
    }

    private class FloatingRecordButton internal constructor(
        context: Context,
        private val recordButtonFab: ImageView,
        private val containerLock: ConstraintLayout?
    ) {
        private var startPositionX = 0f
        private var startPositionY = 0f
        var lastOffsetX = 0f
        var lastOffsetY = 0f
        private var context = context

        private val px12Y = Utils.dpToPx(context, 12f).toFloat()
        private val px15X = Utils.dpToPx(context, 15f).toFloat()

        fun display(x: Float, y: Float) {
            startPositionX = 0f
            startPositionY = 0f
            recordButtonFab.visibility = View.VISIBLE

            val animation = AnimationSet(true)
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
            containerLock?.isVisible = true
        }

        fun moveTo(x: Float, y: Float) {
            lastOffsetX = getXOffset(x)
            lastOffsetY = getYOffset(y)
            if (Math.abs(lastOffsetX) > Math.abs(lastOffsetY)) {
                lastOffsetY = Utils.dpToPx(context, 12f).toFloat()
            } else {
                lastOffsetX = Utils.dpToPx(context, 15f).toFloat()
            }
            recordButtonFab.translationX = lastOffsetX
            recordButtonFab.translationY = lastOffsetY
        }

        fun hide() {
            if (recordButtonFab.visibility != View.VISIBLE) return
            recordButtonFab.translationX = px15X
            recordButtonFab.translationY = px12Y
            val animation = AnimationSet(false)
            val scaleAnimation: Animation = ScaleAnimation(
                1f, 0.5f, 1f, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
            )
            val translateAnimation: Animation = TranslateAnimation(
                Animation.ABSOLUTE, lastOffsetX,
                Animation.ABSOLUTE, px15X,
                Animation.ABSOLUTE, lastOffsetY,
                Animation.ABSOLUTE, px12Y
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
            containerLock?.isVisible = false
        }

        private fun getXOffset(x: Float): Float {
            return if (ViewCompat.getLayoutDirection(recordButtonFab) == ViewCompat.LAYOUT_DIRECTION_LTR) -Math.max(
                0f,
                startPositionX - x
            ) else Math.max(px15X, x - startPositionX)
        }

        private fun getYOffset(y: Float): Float {
            return Math.min(px12Y, y - startPositionY)
        }
    }

    override fun setListener(listener: Listener) {
        this.mListener = listener
    }

    override fun cancelAction() {
        if (state != State.NOT_RUNNING) {
            state = State.NOT_RUNNING
            hideUi()
            mListener?.onRecordCanceled()
        }
    }

    override fun isRecordingLocked(): Boolean {
        return state == State.RUNNING_LOCKED
    }

    override fun unlockAction() {
        if (state == State.RUNNING_LOCKED) {
            state = State.NOT_RUNNING
            binding.quickAudioFab.isVisible = false
            hideUi()
            mListener?.onRecordReleased()
        }
    }
}