package com.naposystems.pepito.ui.custom.inputPanel

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.InputPanelQuoteBinding
import com.naposystems.pepito.entity.message.MessageAndAttachment

class InputPanelQuote(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

    private var binding: InputPanelQuoteBinding

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.Quote,
            0, 0
        ).apply {
            try {
                val infService = Context.LAYOUT_INFLATER_SERVICE
                val layoutInflater = getContext().getSystemService(infService) as LayoutInflater
                binding = DataBindingUtil.inflate(
                    layoutInflater,
                    R.layout.input_panel_quote,
                    this@InputPanelQuote,
                    true
                )

                binding.imageButtonCloseQuote.setOnClickListener {
                    binding.containerQuote.visibility = View.GONE
                    binding.imageViewQuote.apply {
                        this.setImageBitmap(null)
                        this.visibility = View.GONE
                    }
                }
            } finally {
                recycle()
            }
        }
    }

    fun setupMessageAndAttachment(messageAndAttachment: MessageAndAttachment) {
        binding.messageAndAttachment = messageAndAttachment
        binding.executePendingBindings()
    }

    fun openQuote() {
        binding.containerQuote.visibility = View.VISIBLE
    }

    fun resetImage() {
        binding.imageViewQuote.apply {
            this.setImageBitmap(null)
            this.visibility = View.GONE
        }
    }
}
