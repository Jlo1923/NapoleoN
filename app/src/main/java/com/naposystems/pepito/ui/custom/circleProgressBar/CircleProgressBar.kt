package com.naposystems.pepito.ui.custom.circleProgressBar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.naposystems.pepito.R
import kotlin.math.min
import kotlin.math.roundToInt


class CircleProgressBar constructor(context: Context, attrs: AttributeSet) : View(context, attrs),
    IContractCircleProgressBar {

    /**
     * ProgressBar's line thickness
     */
    private var mStrokeWidth: Int = 0
    private var mProgress = 0f
    private var mMin = 0
    private var mMax = 100

    /**
     * Start the progress at 12 o'clock
     */
    private val mStartAngle = -90f
    private var mColor: Int = R.color.black
    private var mRectF: RectF = RectF()
    private val mBackgroundPaint: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG)
    }
    private val mForegroundPaint: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG)
    }

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CircleProgressBar,
            0, 0
        ).apply {
            try {
                mStrokeWidth = getDimensionPixelSize(
                    R.styleable.CircleProgressBar_progressBarThickness,
                    0
                )
                mProgress = getFloat(R.styleable.CircleProgressBar_progress, 0f)
                mColor =
                    getResourceId(R.styleable.CircleProgressBar_progressbarColor, R.color.black)
                mMin = getInt(R.styleable.CircleProgressBar_min, 0)
                mMax = getInt(R.styleable.CircleProgressBar_max, 100)

                mBackgroundPaint.apply {
                    color = adjustAlpha(mColor, 0.3f)
                    style = Paint.Style.STROKE
                    strokeWidth = mStrokeWidth.toFloat()
                }

                mForegroundPaint.apply {
                    color = resources.getColor(mColor, context.theme)
                    style = Paint.Style.STROKE
                    strokeWidth = mStrokeWidth.toFloat()
                }

            } finally {
                recycle()
            }

        }
    }

    private fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = (Color.alpha(color) * factor).roundToInt()
        val red: Int = Color.red(color)
        val green: Int = Color.green(color)
        val blue: Int = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height = getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)
        val width = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        val min = min(width, height)
        setMeasuredDimension(min, min)
        mRectF.set(
            (0 + mStrokeWidth / 2).toFloat(),
            (0 + mStrokeWidth / 2).toFloat(),
            (min - mStrokeWidth / 2).toFloat(),
            (min - mStrokeWidth / 2).toFloat()
        )
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas!!.drawOval(mRectF, mBackgroundPaint)
        val angle: Float = 360 * mProgress / mMax
        canvas.drawArc(mRectF, mStartAngle, angle, false, mForegroundPaint)
    }

    //region Implementation IContractCircleProgressBar

    override fun getProgress() = this.mProgress

    override fun setProgressColor(colorId: Int) {
        this.mColor = colorId
        mForegroundPaint.apply {
            color = resources.getColor(mColor, context.theme)
            style = Paint.Style.STROKE
            strokeWidth = mStrokeWidth.toFloat()
        }
        invalidate()
    }

    override fun setProgress(progress: Float) {
        this.mProgress = progress
        invalidate()
    }
    //endregion
}