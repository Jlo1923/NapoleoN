package com.naposystems.napoleonchat.ui.custom

import android.animation.Animator
import android.content.Context
import android.graphics.Typeface
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Utils
import kotlin.math.hypot


class SearchView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private var customHint: String? = ""
    private var characterCount: Int = 0
    private var isOpen: Boolean = false
    private var listener: OnSearchView?=null
    private var location: Int = 0

    private val normalMargin by lazy {
        resources.getDimension(R.dimen.normal_margin).toInt()
    }

    private val imageButtonClose by lazy {
        AppCompatImageButton(context)
    }

    private val textViewIndicator by lazy {
        AppCompatTextView(context)
    }

    private lateinit var textInput: TextView

    interface OnSearchView {
        fun onOpened()
        fun onQuery(text: String)
        fun onClosed()
        fun onClosedCompleted()
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

            } finally {
                recycle()
            }
        }
    }

    private fun createTextInput() {
        textInput = if (location == 0) {
            TextInputEditText(ContextThemeWrapper(context, R.style.SearchStyle))
        } else {
            TextInputEditText(ContextThemeWrapper(context, R.style.SearchStyleLocation))
        }
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
            override fun afterTextChanged(s: Editable?) = Unit

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                listener?.onQuery(s.toString())
            }
        })

        addView(textInput)

        with(textInput) {
            val linearLayoutParams = LayoutParams(layoutParams)

            linearLayoutParams.apply {
                width = 0
                weight = 1f
                if (location == 0) setMargins(4, 0, normalMargin, 0)
                else setMargins(normalMargin, 0, normalMargin, 0)
            }

            layoutParams = linearLayoutParams
        }
    }

    fun setTextSearch(text: String) {
        textInput.text = text
    }

    private fun createImageViewIndicator() {
        val outValue = TypedValue()
        context.theme.resolveAttribute(
            android.R.attr.selectableItemBackgroundBorderless,
            outValue,
            true
        )

        textViewIndicator.apply {
            text = "@"
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(ContextCompat.getColor(context, R.color.secondaryTextColor))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            setBackgroundResource(outValue.resourceId)
        }

        addView(textViewIndicator)

        with(textViewIndicator) {
            val linearLayoutParams = LayoutParams(layoutParams)

            linearLayoutParams.apply {
                setMargins(normalMargin, 0, 0, 8)
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
                    listener?.onClosed()
                    visibility = View.GONE
                    isOpen = false
                    textInput.text = ""
                    Utils.hideKeyboard(textInput)
                } else {
                    isOpen = true
                    listener?.onOpened()
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

    fun close(location: Int = 0) {
        if (isOpen) {
            val x = imageButtonClose.right - (imageButtonClose.width / 2)
            val y = imageButtonClose.bottom - (imageButtonClose.height / 2)

            val endRadius = 0.0f
            val anim: Animator

            val startRadius: Float = hypot(
                this.width.toDouble(),
                this.height.toDouble()
            ).toFloat()

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
                        visibility = View.GONE
                        isOpen = false
                        textInput.text = ""
                        Utils.hideKeyboard(textInput)
                        if (location == Constants.LocationAddContact.CONTACTS.location) {
                            listener?.onClosedCompleted()
                        }
                        listener?.onClosed()
                    } else {
                        isOpen = true
                        listener?.onOpened()
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
    }

    fun setListener(listener: OnSearchView?) {
        this.listener = listener
    }

    fun setHint(resourceId: Int) {
        textInput.setHint(resourceId)
        invalidate()
    }

    fun setStyleable(location: Int = 0) {
        this.location = location
        removeAllViews()
        if (location == 0) createImageViewIndicator()
        createTextInput()
        createImageButtonClose()

    }

    fun setMenuItem(menuItem: MenuItem) {
        menuItem.setOnMenuItemClickListener {
            showSearchView()
            true
        }
    }

    fun isOpened() = isOpen
}