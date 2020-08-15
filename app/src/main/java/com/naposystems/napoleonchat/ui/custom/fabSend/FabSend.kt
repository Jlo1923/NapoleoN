package com.naposystems.napoleonchat.ui.custom.fabSend

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.Context
import android.os.SystemClock
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.MotionEvent.*
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.CustomViewFabSendBinding
import com.naposystems.napoleonchat.utility.Utils.Companion.setupNotificationSound
import timber.log.Timber
import kotlin.math.roundToLong

class FabSend(context: Context, attrs: AttributeSet) : FloatingActionButton(context, attrs),
    IContractFabSend {

    private var micToSend: AnimatedVectorDrawableCompat? = null
    private var sendToMic: AnimatedVectorDrawableCompat? = null
    private var isShowingMic: Boolean = true
    private var showOnlySendIcon: Boolean = false
    private var mListener: FabSendListener? = null
    private var mContainerLock: CustomViewFabSendBinding? = null

    private var mActivePointerId = INVALID_POINTER_ID
    private var mInitialX: Float = 0f
    private var mInitialY: Float = 0f
    private var mLastTouchX: Float = 0f
    private var mLastTouchY: Float = 0f
    private var mPosX: Float = 0f
    private var mPosY: Float = 0f
    private var mInitialContainerHeight: Int = 0

    private var mIsLongPressed = false
    private var mIsOnlyVertical = false
    private var mIsOnlyHorizontal = false
    private var mHasLocked = false
    private var mHasCancel = false

    interface FabSendListener {
        fun checkRecordAudioPermission(successCallback: () -> Unit)
        fun onMicActionDown()
        fun onMicActionUp(hasLock: Boolean, hasCancel: Boolean)
        fun onMicLocked()
        fun onMicCancel()
    }

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.FabSend,
            0, 0
        ).apply {
            try {

                mInitialX = x
                mInitialY = y

                showOnlySendIcon = getBoolean(R.styleable.FabSend_showOnlySendIcon, false)

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

                setOnLongClickListener {
                    if (!mIsLongPressed) {
                        mIsLongPressed = true
                        Timber.d("longPressed")
                        val downTime: Long = SystemClock.uptimeMillis()
                        val eventTime: Long = SystemClock.uptimeMillis() + 100
                        val x = 0.0f
                        val y = 0.0f
                        val metaState = 0
                        val motionEvent = obtain(
                            downTime,
                            eventTime,
                            ACTION_DOWN,
                            x,
                            y,
                            metaState
                        )

                        dispatchTouchEvent(motionEvent)
                    }
                    true
                }

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

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        super.onTouchEvent(ev)
        if (isShowingMic) {
            mListener?.checkRecordAudioPermission() {
                if (mIsLongPressed) {
                    when (ev.actionMasked) {
                        ACTION_CANCEL -> {
                            Timber.d("ACTION_CANCEL")
                            actionCancel()
                        }
                        ACTION_DOWN -> {
                            setupNotificationSound(context, R.raw.tone_start_recording_audio)
                            Timber.d("ACTION_DOWN")
                            actionDown(ev)
                        }
                        ACTION_MOVE -> {
                            actionMove(ev)
                        }
                        ACTION_UP -> {
                            Timber.d("ACTION_UP")
                            actionUp()
                            performClick()
                        }
                        ACTION_POINTER_UP -> {
                            Timber.d("ACTION_POINTER_UP")
                            actionPointerUp(ev)
                        }
                    }
                }
            }
        }
        return true
    }

    private fun actionCancel() {
        if (mHasLocked) {
            mListener?.onMicLocked()
        }

        if (mHasCancel) {
            mListener?.onMicCancel()
            mHasCancel = false
        }
    }

    private fun actionPointerUp(ev: MotionEvent) {
        ev.actionIndex.also { pointerIndex ->
            ev.getPointerId(pointerIndex)
                .takeIf { it == mActivePointerId }
                ?.run {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    val newPointerIndex = if (pointerIndex == 0) 1 else 0
                    mLastTouchX = ev.getX(newPointerIndex)
                    mLastTouchY = ev.getY(newPointerIndex)
                    mActivePointerId =
                        ev.getPointerId(newPointerIndex)
                }
        }
    }

    private fun actionDown(ev: MotionEvent) {
        if (isShowingMic) {
            mInitialX = this.x
            mInitialY = this.y

            val objectAnimator = AnimatorInflater.loadAnimator(
                context,
                R.animator.animator_fab_record_audio
            ) as AnimatorSet
            objectAnimator.setTarget(this)
            objectAnimator.doOnEnd {
                mListener?.onMicActionDown()
            }
            objectAnimator.start()

            ev.actionIndex.also { pointerIndex ->
                // Remember where we started (for dragging)
                mLastTouchX = ev.getX(pointerIndex)
                mLastTouchY = ev.getY(pointerIndex)
            }

            // Save the ID of this pointer (for dragging)
            mActivePointerId = ev.getPointerId(0)
        }
    }

    private fun actionUp() {
        if (isShowingMic) {
            mIsLongPressed = false
            mActivePointerId = INVALID_POINTER_ID
            mPosX = 0f
            mPosY = 0f
            mLastTouchX = 0f
            mLastTouchY = 0f
            if (mInitialX != 0f && mInitialY != 0f) {
                this.x = mInitialX
                this.y = mInitialY
            }

            val objectAnimator = AnimatorInflater.loadAnimator(
                context,
                R.animator.animator_fab_record_audio_reverse
            ) as AnimatorSet
            objectAnimator.setTarget(this)
            objectAnimator.start()
            mListener?.onMicActionUp(mHasLocked, mHasCancel)
        }
    }

    private fun actionMove(ev: MotionEvent): Boolean {
        if (isShowingMic && mInitialContainerHeight > 0 && mActivePointerId != INVALID_POINTER_ID) {
            // Find the index of the active pointer and fetch its position
            val (x: Float, y: Float) = ev.findPointerIndex(mActivePointerId)
                .let { pointerIndex ->
                    // Calculate the distance moved
                    ev.getX(pointerIndex) to ev.getY(pointerIndex)
                }

            mPosX += x - mLastTouchX
            mPosY += y - mLastTouchY

            val futureX = this.x + mPosX
            val futureY = this.y + mPosY

            if (futureX < mInitialX && !mIsOnlyVertical) {
                moveHorizontal(futureX)
            } else if (futureY < mInitialY && !mIsOnlyHorizontal) {
                moveVertical(futureY)
            } else {
                mIsOnlyHorizontal = false
                mIsOnlyVertical = false
            }

            invalidate()

            // Remember this touch position for the next move event
            mLastTouchX = x
            mLastTouchY = y
            return true
        } else
            return false
    }

    private fun moveVertical(futureY: Float) {
        mIsOnlyVertical = true
        mIsOnlyHorizontal = false

        mContainerLock?.container?.let { container ->
            val maxY = container.y
            val maximo = mInitialY - container.y
            val containerLockLayoutParams = container.layoutParams

            if (futureY > maxY) {
                val difference = mInitialY - futureY
                val percentage = difference / maximo

                val finalPercentage = (percentage * 100.0).roundToLong() / 100.0

                //Timber.d("mInitialY: ${mInitialY}, difference: ${difference}, percentage: ${finalPercentage}")
                val scale = 2 - finalPercentage

                if (finalPercentage < 1 && scale > 0) {
                    this.scaleX = scale.toFloat()
                    this.scaleY = scale.toFloat()

                    val finalHeight =
                        context.resources.getDimension(R.dimen.conversation_fab_size)
                    val newHeight =
                        (finalHeight - mInitialContainerHeight) * finalPercentage + mInitialContainerHeight
                    containerLockLayoutParams.height = newHeight.toInt()
                    container.layoutParams = containerLockLayoutParams

                }
                this.y += mPosY
            } else {
                mHasLocked = true
                containerLockLayoutParams.height =
                    context.resources.getDimension(R.dimen.conversation_fab_size)
                        .toInt()
                container.layoutParams = containerLockLayoutParams

                this.x = mInitialX
                this.y = mInitialY
                dispatchActionUp()
                morphToSend()
            }
        }
    }

    private fun moveHorizontal(futureX: Float) {
        mIsOnlyHorizontal = true
        mIsOnlyVertical = false

        val maxX = mInitialX * 0.5

        if (futureX > maxX) {
            this.x += mPosX
        } else {
            this.mHasCancel = true
            this.scaleX = 1.0f
            this.scaleY = 1.0f

            this.x = mInitialX
            this.y = mInitialY
            dispatchActionUp()
        }
    }

    private fun dispatchActionUp() {
        val downTime: Long = SystemClock.uptimeMillis()
        val eventTime: Long = SystemClock.uptimeMillis() + 100
        val x = 0.0f
        val y = 0.0f
        val metaState = 0
        val motionEvent = obtain(
            downTime,
            eventTime,
            ACTION_CANCEL,
            x,
            y,
            metaState
        )

        dispatchTouchEvent(motionEvent)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

//region Implementation IContractFabSend

    override fun setListener(listener: FabSendListener) {
        this.mListener = listener
    }

    override fun morphToSend() {
        if (isShowingMic) {
            Timber.d("morphToSend")
            val drawable = micToSend
            setImageDrawable(drawable)
            drawable?.start()
            this.scaleX = 1.0f
            this.scaleY = 1.0f
            isShowingMic = false
        }
    }

    override fun morphToMic() {
        if (!isShowingMic) {
            Timber.d("morphToMic")
            val drawable = sendToMic
            setImageDrawable(drawable)
            drawable?.start()
            this.scaleX = 1.0f
            this.scaleY = 1.0f
            isShowingMic = true
        }
    }

    override fun isShowingMic() = isShowingMic

    override fun setContainerLock(constraintLayout: CustomViewFabSendBinding) {
        this.mContainerLock = constraintLayout
        constraintLayout.container.post {
            mInitialContainerHeight = constraintLayout.container.height
        }
    }

    override fun isLocked() = mHasLocked

    override fun reset() {
        this.mHasLocked = false
    }

//endregion
}