package com.naposystems.pepito.ui.custom.cameraButton

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.Context
import android.os.SystemClock
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageButton
import com.naposystems.pepito.R
import timber.log.Timber
import kotlin.math.roundToLong

class CameraButton(context: Context, attributeSet: AttributeSet) :
    AppCompatImageButton(context, attributeSet), IContractCameraButton {

    private var mAllowSlide: Boolean = true
    private var mListener: CameraButtonListener? = null

    private var mActivePointerId = MotionEvent.INVALID_POINTER_ID
    private var mInitialX: Float = 0f
    private var mInitialY: Float = 0f
    private var mLastTouchX: Float = 0f
    private var mLastTouchY: Float = 0f
    private var mPosX: Float = 0f
    private var mPosY: Float = 0f
    private var mMaxY: Float = 0f
    private var mInitialContainerHeight: Int = 0

    private var mIsLongPressed = false
    private var mIsOnlyVertical = false
    private var mIsOnlyHorizontal = false
    private var mHasLocked = false
    private var mHasCancel = false

    interface CameraButtonListener {
        fun startToRecord()
        fun hasLocked()
        fun actionUp(hasLocked: Boolean)
    }

    init {
        context.theme.obtainStyledAttributes(
            attributeSet,
            R.styleable.CameraButton,
            0, 0
        ).apply {
            try {

                mInitialX = x
                mInitialY = y

                mAllowSlide = getBoolean(R.styleable.CameraButton_allowSlide, true)

                setOnLongClickListener {
                    if (!mIsLongPressed) {
                        mIsLongPressed = true
                        Timber.d("longPressed")
                        val downTime: Long = SystemClock.uptimeMillis()
                        val eventTime: Long = SystemClock.uptimeMillis() + 100
                        val x = 0.0f
                        val y = 0.0f
                        val metaState = 0
                        val motionEvent = MotionEvent.obtain(
                            downTime,
                            eventTime,
                            MotionEvent.ACTION_DOWN,
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

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        super.onTouchEvent(ev)
        if (mIsLongPressed) {
            when (ev.actionMasked) {
                MotionEvent.ACTION_CANCEL -> {
                    Timber.d("ACTION_CANCEL")
                    actionCancel()
                }
                MotionEvent.ACTION_DOWN -> {
                    Timber.d("ACTION_DOWN")
                    actionDown(ev)
                }
                MotionEvent.ACTION_MOVE -> {
                    if (mMaxY > 0 && mAllowSlide && !mHasLocked) {
                        actionMove(ev)
                    }
                }
                MotionEvent.ACTION_UP -> {
                    Timber.d("ACTION_UP")
                    actionUp()
//                    performClick()
                }
                MotionEvent.ACTION_POINTER_UP -> {
                    Timber.d("ACTION_POINTER_UP")
                    actionPointerUp(ev)
                }
            }
        }
        return true
    }

    private fun actionCancel() {
        if (mHasLocked) {
            mListener?.hasLocked()
        }

        if (mHasCancel) {
//            mListener?.onMicCancel()
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
        mInitialX = this.x
        mInitialY = this.y

        /*val objectAnimator = AnimatorInflater.loadAnimator(
            context,
            R.animator.animator_fab_record_audio
        ) as AnimatorSet
        objectAnimator.setTarget(this)
        objectAnimator.doOnEnd {
            mListener?.onMicActionDown()
        }
        objectAnimator.start()*/

        ev.actionIndex.also { pointerIndex ->
            // Remember where we started (for dragging)
            mLastTouchX = ev.getX(pointerIndex)
            mLastTouchY = ev.getY(pointerIndex)
        }

        // Save the ID of this pointer (for dragging)
        mActivePointerId = ev.getPointerId(0)

        if (mIsLongPressed) {
            mListener?.startToRecord()
        }
    }

    private fun actionUp() {
        mIsLongPressed = false
        mActivePointerId = MotionEvent.INVALID_POINTER_ID
        mPosX = 0f
        mPosY = 0f
        mLastTouchX = 0f
        mLastTouchY = 0f
        if (mInitialX != 0f && mInitialY != 0f) {
            this.x = mInitialX
            this.y = mInitialY
        }
        mListener?.actionUp(mHasLocked)
    }

    private fun actionMove(event: MotionEvent): Boolean {
        if (mActivePointerId != MotionEvent.INVALID_POINTER_ID) {
            val y = event.findPointerIndex(mActivePointerId).let { pointerIndex ->
                event.getY(pointerIndex)
            }

            mPosY += y - mLastTouchY

            val futureY = this.y + mPosY

            if (futureY < mInitialY) {
                Timber.d("futureY: $futureY, mInitialY: $mInitialY, mMaxY: $mMaxY")
                val maximo = mInitialY - (200f)

                if (futureY > mMaxY) {
                    val difference = mInitialY - futureY
                    val percentage = difference / maximo

                    val finalPercentage = (percentage * 100.0).roundToLong() / 100.0
                    Timber.d("finalPercentage: ${finalPercentage.toFloat()}")
//                    mListener?.onSlide(finalPercentage.toFloat())
                    this.y += mPosY
                } else {
                    mHasLocked = true
                    this.y = mInitialY
                    dispatchActionCancel()
                }
            }

            invalidate()

            // Remember this touch position for the next move event
            mLastTouchX = x
            mLastTouchY = y
            return true
        } else
            return false
    }

    private fun dispatchActionCancel() {
        val downTime: Long = SystemClock.uptimeMillis()
        val eventTime: Long = SystemClock.uptimeMillis() + 100
        val x = 0.0f
        val y = 0.0f
        val metaState = 0
        val motionEvent = MotionEvent.obtain(
            downTime,
            eventTime,
            MotionEvent.ACTION_CANCEL,
            x,
            y,
            metaState
        )

        dispatchTouchEvent(motionEvent)
    }

    //region Implementation IContractCameraButton

    override fun setAllowSlide(allowSlide: Boolean) {
        this.mAllowSlide = allowSlide
    }

    override fun setListener(cameraButtonListener: CameraButtonListener) {
        this.mListener = cameraButtonListener
    }

    override fun setMaxY(maxY: Float) {
        this.mMaxY = maxY
    }

    override fun isLocked() = mHasLocked

    //endregion
}