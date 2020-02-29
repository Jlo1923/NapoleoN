package com.naposystems.pepito.ui.custom

import android.animation.Animator
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.ViewAnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageButton
import com.google.android.material.textfield.TextInputEditText
import com.naposystems.pepito.R
import com.naposystems.pepito.utility.Utils
import kotlin.math.hypot

class SearchView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private var customHint: String? = ""
    private var characterCount: Int = 0
    private var isOpen: Boolean = false
    private lateinit var listener: OnSearchView

    private val normalMargin by lazy {
        resources.getDimension(R.dimen.normal_margin).toInt()
    }

    private val imageButtonClose by lazy {
        AppCompatImageButton(context)
    }

    private val textInput by lazy {
        TextInputEditText(context)
    }

    interface OnSearchView {
        fun onOpened()
        fun onQuery(text: String)
        fun onClosed()
    }

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.SearchView,
            0, 0
        ).apply {
            try {
                orientation = HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                visibility = View.GONE
                setBackgroundResource(R.color.white)

                customHint = getString(R.styleable.SearchView_hint)
                characterCount = getInt(R.styleable.SearchView_characterCount, 4)

                createTextInput()

                createImageButtonClose()

            } finally {
                recycle()
            }
        }
    }

    private fun createTextInput() {
        textInput.apply {
            setBackgroundColor(
                resources.getColor(
                    android.R.color.transparent,
                    context.theme
                )
            )
            setTextColor(resources.getColor(R.color.searchViewTextColor, context.theme))
            setHintTextColor(resources.getColor(R.color.searchViewHintColor, context.theme))
            hint = customHint
            maxLines = 1
            isSingleLine = true
            imeOptions = EditorInfo.IME_ACTION_SEARCH
            isFocusable = true
            isFocusableInTouchMode = true
        }

        textInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                //nothing
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //nothing
                listener.onQuery(s.toString())
            }
        })

        addView(textInput)

        with(textInput) {
            val linearLayoutParams = LayoutParams(layoutParams)

            linearLayoutParams.apply {
                width = 0
                weight = 1f
                setMargins(normalMargin, 0, normalMargin, 0)
            }

            layoutParams = linearLayoutParams
        }
    }

    private fun createImageButtonClose() {
        imageButtonClose.setOnClickListener {
            //            isOpen = true
            showSearchView()
        }

        val outValue = TypedValue()
        context.theme.resolveAttribute(
            android.R.attr.selectableItemBackgroundBorderless,
            outValue,
            true
        )

        imageButtonClose.apply {
            setImageResource(R.drawable.ic_close_black_24)
            setBackgroundResource(outValue.resourceId)
        }

        addView(imageButtonClose)

        with(imageButtonClose) {
            val linearLayoutParams = LayoutParams(layoutParams)

            linearLayoutParams.apply {
                setMargins(0, 0, normalMargin, 0)
            }

            layoutParams = linearLayoutParams
        }
    }

    fun showSearchView() {
        val x = imageButtonClose.right - (imageButtonClose.width / 2)
        val y = imageButtonClose.bottom - (imageButtonClose.height / 2)

        var startRadius = 0.0f
        var endRadius = 0.0f
        val anim: Animator

        if (!isOpen) {
            endRadius = hypot(
                this.width.toDouble(),
                this.height.toDouble()
            ).toFloat()
        } else {
            startRadius = hypot(
                this.width.toDouble(),
                this.height.toDouble()
            ).toFloat()
        }

        anim = ViewAnimationUtils.createCircularReveal(
            this,
            x,
            y,
            startRadius,
            endRadius
        )

        anim.duration = 500

        anim.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
                //nothing
            }

            override fun onAnimationEnd(animation: Animator?) {
                if (isOpen) {
                    listener.onClosed()
                    visibility = View.GONE
                    isOpen = false
                    textInput.setText("")
                    Utils.hideKeyboard(textInput)
                } else {
                    isOpen = true
                    listener.onOpened()
                }
            }

            override fun onAnimationCancel(animation: Animator?) {
                //nothing
            }

            override fun onAnimationStart(animation: Animator?) {
                //nothing
            }
        })

        visibility = View.VISIBLE

        textInput.requestFocus()
        Utils.openKeyboard(textInput)
        anim.start()
    }

    fun setListener(listener: OnSearchView) {
        this.listener = listener
    }

    fun setHint(resourceId: Int) {
        textInput.setHint(resourceId)
        invalidate()
    }

    fun setMenuItem(menuItem: MenuItem) {
        menuItem.setOnMenuItemClickListener {
            showSearchView()
            true
        }
    }

    fun isOpened() = isOpen
}