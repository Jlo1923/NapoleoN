package com.naposystems.napoleonchat.ui.custom.customVerticalSeekbar

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import timber.log.Timber

class CustomVerticalSeekBar constructor(context: Context, attributeSet: AttributeSet) :
    androidx.appcompat.widget.AppCompatSeekBar(context, attributeSet) {

    private lateinit var listener: Listener

    interface Listener {
        fun onSlide(zoomValue: Float)
    }

    fun setListener(listener: Listener  ) {
        this.listener = listener
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(h, w, oldh, oldw)
    }

    @Synchronized
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec)
        setMeasuredDimension(measuredHeight, measuredWidth)
    }

    override fun onDraw(c: Canvas) {
        c.rotate(-90f)
        c.translate(-height.toFloat(), 0f)
        super.onDraw(c)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP -> {
                progress = max - (max * event.y / height).toInt()
                onSizeChanged(width, height, 0, 0)

                val zoomValue = progress * 0.01f
                listener.onSlide(zoomValue)
            }
            MotionEvent.ACTION_CANCEL -> Unit
        }
        return true
    }
}
