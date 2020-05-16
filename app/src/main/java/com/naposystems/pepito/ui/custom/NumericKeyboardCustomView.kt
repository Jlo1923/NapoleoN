package com.naposystems.pepito.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent.*
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.CustomViewNumericKeyboardBinding

class NumericKeyboardCustomView(context: Context, attrs: AttributeSet) :
    LinearLayout(context, attrs) {

    private var binding: CustomViewNumericKeyboardBinding
    private var showBiometric: Boolean = false
    private lateinit var mListener: OnEventListener


    interface OnEventListener {
        fun onKeyPressed(keyCode: Int)
        fun onDeletePressed()
    }

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.NumericKeyboardCustomView,
            0, 0
        ).apply {
            try {
                val infService = Context.LAYOUT_INFLATER_SERVICE
                val layoutInflater = getContext().getSystemService(infService) as LayoutInflater
                binding = DataBindingUtil.inflate(
                    layoutInflater,
                    R.layout.custom_view_numeric_keyboard,
                    this@NumericKeyboardCustomView,
                    true
                )

                showBiometric = getBoolean(
                    R.styleable.NumericKeyboardCustomView_showBiometric,
                    false
                )

                setKeyListener()

            } finally {
                recycle()
            }
        }
    }

    fun setListener(listener: OnEventListener) {
        this.mListener = listener
    }

    fun enableKeyboard() {
        binding.button0.isEnabled = true
        binding.button1.isEnabled = true
        binding.button2.isEnabled = true
        binding.button3.isEnabled = true
        binding.button4.isEnabled = true
        binding.button5.isEnabled = true
        binding.button6.isEnabled = true
        binding.button7.isEnabled = true
        binding.button8.isEnabled = true
        binding.button9.isEnabled = true
        binding.buttonDelete.isEnabled = true

    }

    fun disableKeyboard() {
        binding.button0.isEnabled = false
        binding.button1.isEnabled = false
        binding.button2.isEnabled = false
        binding.button3.isEnabled = false
        binding.button4.isEnabled = false
        binding.button5.isEnabled = false
        binding.button6.isEnabled = false
        binding.button7.isEnabled = false
        binding.button8.isEnabled = false
        binding.button9.isEnabled = false
        binding.buttonDelete.isEnabled = false

    }

    private fun setKeyListener() {
        binding.button0.setOnClickListener {
            mListener.onKeyPressed(0)
        }

        binding.button1.setOnClickListener {
            mListener.onKeyPressed(1)
        }

        binding.button2.setOnClickListener {
            mListener.onKeyPressed(2)
        }

        binding.button3.setOnClickListener {
            mListener.onKeyPressed(3)
        }

        binding.button4.setOnClickListener {
            mListener.onKeyPressed(4)
        }

        binding.button5.setOnClickListener {
            mListener.onKeyPressed(5)
        }

        binding.button6.setOnClickListener {
            mListener.onKeyPressed(6)
        }

        binding.button7.setOnClickListener {
            mListener.onKeyPressed(7)
        }

        binding.button8.setOnClickListener {
            mListener.onKeyPressed(8)
        }

        binding.button9.setOnClickListener {
            mListener.onKeyPressed(9)
        }

        binding.buttonDelete.setOnClickListener {
            mListener.onDeletePressed()
        }
    }
}