package com.naposystems.napoleonchat.ui.custom.numericKeyboard

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.CustomViewNumericKeyboardBinding

class NumericKeyboardCustomView(context: Context, attrs: AttributeSet) :
    LinearLayout(context, attrs), IContractNumericKeyboard {

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

    //region Implementation IContractNumericKeyboard
    override fun setListener(listener: OnEventListener) {
        this.mListener = listener
    }

    override fun showDeleteKey(show: Boolean) {
        binding.buttonDelete.visibility = if (show) View.VISIBLE else View.GONE
    }

    //endregion
}