package com.naposystems.pepito.ui.custom

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.view.ContextThemeWrapper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.LinearLayout.HORIZONTAL
import android.widget.RelativeLayout
import android.widget.TextView
import com.naposystems.pepito.R
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.Utils
import timber.log.Timber


class EnterCodeWidget(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

    private var position: Int = 0
    private var numBoxes: Int = 0
    private var isSecureText: Boolean = false
    private var errorText: String = ""
    private var isErrorShowing: Boolean = false
    private val textViews: ArrayList<TextView> = ArrayList()
    private lateinit var mListener: OnEventListener

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
                //Obtenemos los atributos ingresados en el .xml
                numBoxes = getInt(R.styleable.EnterCodeWidget_numBoxes, 0)
                isSecureText = getBoolean(R.styleable.EnterCodeWidget_isSecureText, false)
                getString(R.styleable.EnterCodeWidget_errorText)?.let { errorText = it }

                createLinearLayout(context)

                createTextViews(context)

                createTextViewError(context)
            } catch (e: Exception) {
                Timber.e(e)
            } finally {
                recycle()
            }
        }
    }

    /**
     * Creamos el LinearLayout horizontal que contendrá los TextViews
     */
    private fun createLinearLayout(context: Context): LinearLayout {
        val colorBackground =
            Utils.convertAttrToColorResource(context, R.attr.attrBackgroundColorBackground)

        linearTextInputs.apply {
            id = View.generateViewId()
            setBackgroundColor(colorBackground)
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
     * return the code entered by the user
     */
    fun getCode(): String {
        var code = ""
        if (textViews.isNotEmpty()) {
            textViews.forEach { textView ->
                code += textView.text
            }
        }

        return code
    }

    fun setAddNumber(keyCode: Int) {
        if (position < numBoxes) {
            focusAfter(keyCode)
        }
    }

    fun deleteNumber() {
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

    fun showError() {
        isErrorShowing = true
        if (textViewError.text != "") textViewError.visibility = View.VISIBLE
        changeBackgroundAllTextInputs(R.drawable.bg_enter_code_error)
        invalidate()
        requestLayout()
        linearTextInputs.startAnimation(
            AnimationUtils.loadAnimation(context, R.anim.shake)
        )
        Utils.vibratePhone(context, Constants.Vibrate.DEFAULT.type)
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
        isErrorShowing = false
        position = 0
        textViewError.visibility = View.GONE
        isErrorShowing = false
    }

    private fun focusAfter(keyCode: Int) {
        val context = textViews[position].context
        textViews[position].text = keyCode.toString()
        textViews[position].background =
            resources.getDrawable(
                R.drawable.bg_enter_code_filled,
                context!!.theme
            )
        position++

        if (position == numBoxes) mListener.onCodeCompleted(true)
    }

    private fun focusBefore() {
        position--
        textViews[position].text = ""
        textViews[position].background =
            resources.getDrawable(R.drawable.bg_enter_code, context!!.theme)
    }
}