package com.naposystems.pepito.ui.custom.inputPanel

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.CustomInputPanelQuoteBinding
import com.naposystems.pepito.entity.message.MessageAndAttachment

class InputPanelQuote(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs),
    IContractInputPanelQuote {

    private var binding: CustomInputPanelQuoteBinding
    private var isCancelable: Boolean = false
    private var isFromInputPanel: Boolean = false
    private var messageAndAttachment: MessageAndAttachment? = null


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
                    R.layout.custom_input_panel_quote,
                    this@InputPanelQuote,
                    true
                )

                isCancelable = getBoolean(R.styleable.Quote_isCancelable, false)
                isFromInputPanel = getBoolean(R.styleable.Quote_isFromInputPanel, false)

                binding.isFromInputPanel = isFromInputPanel


                if (isCancelable) {
                    binding.imageButtonCloseQuote.visibility = View.VISIBLE
                }

                binding.imageButtonCloseQuote.setOnClickListener {
                    closeQuote()
                }
            } finally {
                recycle()
            }
        }
    }

    override fun setupMessageAndAttachment(messageAndAttachment: MessageAndAttachment) {
        this.messageAndAttachment = messageAndAttachment
        binding.messageAndAttachment = messageAndAttachment
        binding.containerQuote.visibility = View.VISIBLE
        binding.executePendingBindings()
    }

    override fun closeQuote() {
        binding.containerQuote.visibility = View.GONE
        binding.imageViewQuote.apply {
            this.setImageBitmap(null)
            this.visibility = View.GONE
        }
        binding.textViewTitleQuote.text = null
        binding.textViewMessageQuote.text = null
        this.messageAndAttachment = null
    }

    override fun resetImage() {
        binding.imageViewQuote.apply {
            this.setImageBitmap(null)
            this.visibility = View.GONE
        }
    }

    override fun getMessageAndAttachment() = this.messageAndAttachment
}
