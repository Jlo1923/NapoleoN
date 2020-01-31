package com.naposystems.pepito.ui.custom

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.InputFilter
import android.text.InputType
import android.util.AttributeSet
import android.view.ContextThemeWrapper
import android.view.KeyCharacterMap
import android.view.KeyEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.LinearLayout.HORIZONTAL
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.naposystems.pepito.R
import com.naposystems.pepito.utility.Utils
import timber.log.Timber


class EnterCodeWidget(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

    private var position: Int = 0
    private var numBoxes: Int = 0
    private var isSecureText: Boolean = false
    private var errorText: String = ""
    private var isErrorShowing: Boolean = false
    private var isTextInputEnabled: Boolean = true
    private val textViews: ArrayList<TextView> = ArrayList()
    private lateinit var mListener: OnEventListener

    private val textInputCode: TextInputEditText by lazy {
        TextInputEditText(context)
    }
    private val linearTextInputs: LinearLayout by lazy {
        LinearLayout(context)
    }
    private val textViewError: TextView by lazy {
        TextView(
            ContextThemeWrapper(context, R.style.SmallBodyTextView_Error)
        )
    }

    interface OnEventListener {
        fun onImeActionDone()
        fun onCodeCompleted(isCompleted: Boolean)
    }

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.EnterCodeWidget,
            0, 0
        ).apply {
            try {
                // Obtenemos los atributos ingresados en el .xml
                numBoxes = getInt(R.styleable.EnterCodeWidget_numBoxes, 0)
                isSecureText = getBoolean(R.styleable.EnterCodeWidget_isSecureText, false)
                errorText = getString(R.styleable.EnterCodeWidget_errorText)!!

                createTextInput()

                createLinearLayout(context)

                createTextViews(context)

                createTextViewError(context)
            } finally {
                recycle()
            }
        }
    }

    /**
     * Creamos el TextInput que manejara el ingreso del código
     */
    private fun createTextInput() {
        if (Build.MANUFACTURER == "samsung")
            textInputCode.inputType = InputType.TYPE_CLASS_PHONE
        else
            textInputCode.inputType = InputType.TYPE_CLASS_NUMBER
        textInputCode.apply {
            val fArray = arrayOfNulls<InputFilter>(1)
            fArray[0] = InputFilter.LengthFilter(numBoxes)

            filters = fArray
            setOnKeyListener(onKeyPressed())
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE && position == numBoxes) {
                    mListener.onImeActionDone()
                    false
                } else {
                    true
                }
            }
        }

        addView(textInputCode)

        with(textInputCode) {
            val layoutParams = LayoutParams(this.layoutParams)

            layoutParams.apply {
                addRule(ALIGN_PARENT_TOP)
                addRule(ALIGN_PARENT_START)
            }

            this.layoutParams = layoutParams
        }
    }

    /**
     * Creamos el LinearLayout horizontal que contendrá los TextViews
     */
    private fun createLinearLayout(context: Context): LinearLayout {

        linearTextInputs.apply {
            id = View.generateViewId()
            setBackgroundColor(resources.getColor(R.color.colorBackground, context.theme))
            orientation = HORIZONTAL
        }

        addView(linearTextInputs)

        with(linearTextInputs) {
            val linearLayoutParams = LayoutParams(layoutParams)

            linearLayoutParams.apply {
                addRule(ALIGN_PARENT_TOP)
                addRule(ALIGN_PARENT_START)
                addRule(ALIGN_PARENT_END)
            }

            layoutParams = linearLayoutParams
        }

        return linearTextInputs
    }

    /**
     * Hacemos un ciclo entre numBoxes y vamos creando y agregando los TextView al
     *  LinearLayout
     */
    private fun createTextViews(context: Context) {
        for (i in 1..numBoxes) {
            val layoutParams = LinearLayout.LayoutParams(0, Utils.dpToPx(context, 70f), 1f)

            if (i < numBoxes) layoutParams.marginEnd =
                Utils.dpToPx(context, 16f)

            val textView = TextView(
                ContextThemeWrapper(
                    context,
                    R.style.TextViewEnterCodeStyle
                )
            )

            textView.inputType =
                if (isSecureText)
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                else
                    InputType.TYPE_CLASS_NUMBER

            textView.setOnClickListener {
                if (isTextInputEnabled) requestFocusFirst()
            }
            textView.layoutParams = layoutParams

            textViews.add(textView)
            linearTextInputs.addView(textView)
        }
    }

    /**
     * Creamos el TextView que mostrará un error
     */

    private fun createTextViewError(context: Context) {
        textViewError.apply {
            text = errorText
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            visibility = View.GONE
        }

        addView(textViewError)

        with(textViewError) {
            val textViewErrorLayoutParams = LayoutParams(layoutParams)

            textViewErrorLayoutParams.apply {
                addRule(BELOW, linearTextInputs.id)
                addRule(ALIGN_PARENT_START)
                addRule(ALIGN_PARENT_END)
                setMargins(0, Utils.dpToPx(context, 16f), 0, 0)
            }

            layoutParams = textViewErrorLayoutParams
        }
    }

    /**
     * Set the listener
     */
    fun setListener(listener: OnEventListener) {
        mListener = listener
    }

    /**
     * This function request the focus to first textInput
     */
    fun requestFocusFirst() {
        textInputCode.isFocusable = true
        textInputCode.isFocusableInTouchMode = true
        textInputCode.requestFocus()
        Utils.openKeyboard(textInputCode)
    }

    /**
     * return the code entered by the user
     */
    fun getCode() = textInputCode.text.toString()

    fun disableTextInput() {
        isTextInputEnabled = false
        textInputCode.isEnabled = false
    }

    fun enableTextInput() {
        isTextInputEnabled = true
        textInputCode.isEnabled = true
        requestFocusFirst()
    }

    fun showError() {
        isErrorShowing = true
        textViewError.visibility = View.VISIBLE
        changeBackgroundAllTextInputs(R.drawable.bg_enter_code_error)
        invalidate()
        requestLayout()
        linearTextInputs.startAnimation(
            AnimationUtils.loadAnimation(context, R.anim.shake)
        )
        vibratePhone()
    }

    private fun changeBackgroundAllTextInputs(drawable: Int) {
        for (textInput in textViews) {
            textInput.background =
                context.resources.getDrawable(drawable, context.theme)
        }
    }

    private fun clearAllTextInputs() {
        for (textInput in textViews) {
            textInput.text = ""
        }
        textInputCode.setText("")
        textInputCode.requestFocus()
        position = 0
        textViewError.visibility = View.GONE
        isErrorShowing = false
    }

    private fun onKeyPressed(): (View, Int, KeyEvent) -> Boolean {
        return { _, keyCode, keyEvent ->
            if (keyEvent.action == KeyEvent.ACTION_UP) {
                when (keyCode) {
                    KeyEvent.KEYCODE_DEL -> {
                        mListener.onCodeCompleted(false)
                        if (isErrorShowing) {
                            changeBackgroundAllTextInputs(R.drawable.bg_enter_code)
                            clearAllTextInputs()
                        } else {
                            if (position > 0) {
                                focusBefore()
                            }
                        }
                    }
                    else -> {
                        if (position < numBoxes) {
                            focusAfter(keyCode)
                        }
                    }
                }
            }
            false
        }
    }

    private fun focusAfter(keyCode: Int) {
        val km = KeyCharacterMap.load(KeyCharacterMap.NUMERIC)
        textViews[position].text = km.getNumber(keyCode).toString()
        textViews[position].background =
            resources.getDrawable(
                R.drawable.bg_enter_code_filled,
                context!!.theme
            )
        textInputCode.requestFocus()
        position++

        if (position == numBoxes) mListener.onCodeCompleted(true)
    }

    private fun focusBefore() {
        position--
        textViews[position].text = ""
        textViews[position].background =
            resources.getDrawable(R.drawable.bg_enter_code, context!!.theme)
        textInputCode.requestFocus()
    }

    private fun vibratePhone() {
        val vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(200)
        }
    }
}