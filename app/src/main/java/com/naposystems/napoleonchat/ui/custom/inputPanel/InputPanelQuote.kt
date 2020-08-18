package com.naposystems.napoleonchat.ui.custom.inputPanel

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.crypto.message.CryptoMessage
import com.naposystems.napoleonchat.databinding.CustomInputPanelQuoteBinding
import com.naposystems.napoleonchat.entity.message.MessageAndAttachment
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Utils

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
        with(messageAndAttachment) {
            this@InputPanelQuote.messageAndAttachment = this
            bindUserBackground(this)
            bindUserQuote(this)
            bindBodyQuote(this)
            bindImageQuote(this)
            bindAttachmentTypeQuote(this)
        }

        binding.executePendingBindings()
    }

    override fun closeQuote() {
        visibility = View.GONE
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

    private fun bindUserBackground(messageAndAttachment: MessageAndAttachment) {
        val quoteNull = messageAndAttachment.quote

        if (isFromInputPanel) {
            messageAndAttachment.message.let { message ->
                binding.container.background = if (message.isMine == Constants.IsMine.YES.value) {
                    context.getDrawable(R.drawable.bg_my_quote_my_message)
                } else {
                    context.getDrawable(R.drawable.bg_your_quote_my_message)
                }

                binding.textViewMessageQuote.setTextColor(
                    if (message.isMine == Constants.IsMine.YES.value) {
                        Utils.convertAttrToColorResource(context, R.attr.attrTextColorBodyMyQuote)
                    } else {
                        Utils.convertAttrToColorResource(context, R.attr.attrTextColorBodyYourQuote)

                    }
                )
            }
        } else {
            quoteNull?.let { quote ->
                binding.container.background = when {
                    quote.isMine == Constants.IsMine.YES.value
                            && messageAndAttachment.message.isMine == 1 -> {
                        context.getDrawable(R.drawable.bg_my_quote_my_message)
                    }
                    quote.isMine == Constants.IsMine.YES.value
                            && messageAndAttachment.message.isMine == 0 -> {
                        context.getDrawable(R.drawable.bg_my_quote_incoming_message)
                    }
                    quote.isMine == Constants.IsMine.NO.value
                            && messageAndAttachment.message.isMine == 1 -> {
                        context.getDrawable(R.drawable.bg_your_quote_my_message)
                    }
                    quote.isMine == Constants.IsMine.NO.value
                            && messageAndAttachment.message.isMine == 0 -> {
                        context.getDrawable(R.drawable.bg_your_quote_incoming_message)
                    }
                    else -> {
                        context.getDrawable(R.drawable.bg_my_quote_my_message)
                    }
                }

                binding.textViewMessageQuote.setTextColor(
                    when {
                        quote.isMine == Constants.IsMine.YES.value
                                && messageAndAttachment.message.isMine == 1 -> {
                            Utils.convertAttrToColorResource(context, R.attr.attrTextColorBodyMyQuote)
                        }
                        quote.isMine == Constants.IsMine.YES.value
                                && messageAndAttachment.message.isMine == 0 -> {
                            Utils.convertAttrToColorResource(context, R.attr.attrTextColorBodyMyQuote)
                        }
                        quote.isMine == Constants.IsMine.NO.value
                                && messageAndAttachment.message.isMine == 1 -> {
                            Utils.convertAttrToColorResource(context, R.attr.attrTextColorBodyYourQuote)
                        }
                        quote.isMine == Constants.IsMine.NO.value
                                && messageAndAttachment.message.isMine == 0 -> {
                            Utils.convertAttrToColorResource(context, R.attr.attrTextColorBodyYourQuote)
                        }
                        else -> {
                            Utils.convertAttrToColorResource(context, R.attr.attrTextColorBodyMyQuote)
                        }
                    }
                )
            }
        }
    }

    private fun bindUserQuote(messageAndAttachment: MessageAndAttachment) {
        var isMineNull: Int? = null

        messageAndAttachment.quote?.let { quote ->
            isMineNull = if (isFromInputPanel) {
                messageAndAttachment.message.isMine
            } else {
                quote.isMine
            }
        } ?: run {
            messageAndAttachment.message.let { message ->
                isMineNull = message.isMine
            }
        }

        val textColorYourName =
            Utils.convertAttrToColorResource(context, R.attr.attrIdentifierColorYourQuote)
        val textColorMyName =
            Utils.convertAttrToColorResource(context, R.attr.attrIdentifierColorMyQuote)

        if (isMineNull == Constants.IsMine.YES.value) {
            binding.textViewTitleQuote.setTextColor(textColorMyName)
            binding.textViewTitleQuote.text = context.getString(R.string.text_you_quote)
        } else {
            val contact = messageAndAttachment.contact
            binding.textViewTitleQuote.setTextColor(textColorYourName)
            binding.textViewTitleQuote.text = contact.let {
                if (contact.nicknameFake.isNotEmpty()) {
                    contact.nicknameFake
                } else {
                    contact.nickname
                }
            }
        }
    }

    private fun bindBodyQuote(
        messageAndAttachment: MessageAndAttachment
    ) {
        val context = binding.textViewMessageQuote.context

        val cryptoMessage = CryptoMessage(context)
        val body = if (isFromInputPanel) {
            val messageNull = messageAndAttachment.message

            messageNull.body
        } else {

            val quoteBody = messageAndAttachment.quote?.body ?: ""

            if (quoteBody.isNotEmpty()) {
                cryptoMessage.decryptMessageBody(quoteBody)
            } else {
                ""
            }
        }

        if (body.isNotEmpty()) {
            binding.textViewMessageQuote.text = body
        }
    }

    private fun bindImageQuote(
        messageAndAttachment: MessageAndAttachment
    ) {

        if (isFromInputPanel) {
            val firstAttachmentNull = messageAndAttachment.getFirstAttachment()

            firstAttachmentNull?.let { attachment ->
                if (attachment.type == Constants.AttachmentType.IMAGE.type) {
                    Glide.with(binding.imageViewQuote)
                        .load(attachment)
                        .transform(CenterCrop(), RoundedCorners(4))
                        .into(binding.imageViewQuote)
                } else if (attachment.type == Constants.AttachmentType.VIDEO.type) {
                    val uri = Utils.getFileUri(
                        binding.imageViewQuote.context,
                        attachment.fileName,
                        Constants.NapoleonCacheDirectories.VIDEOS.folder
                    )
                    Glide.with(binding.imageViewQuote)
                        .load(uri)
                        .thumbnail(0.1f)
                        .transform(CenterCrop(), RoundedCorners(4))
                        .into(binding.imageViewQuote)
                }
                binding.imageViewQuote.visibility = View.VISIBLE
            } ?: run {
                binding.imageViewQuote.visibility = View.GONE
            }
        } else {
            messageAndAttachment.quote?.let { quote ->

                when (quote.attachmentType) {
                    Constants.AttachmentType.IMAGE.type -> {

                        val uri = Utils.getFileUri(
                            binding.imageViewQuote.context,
                            quote.thumbnailUri,
                            Constants.NapoleonCacheDirectories.IMAGES.folder
                        )

                        Glide.with(binding.imageViewQuote)
                            .load(uri)
                            .transform(CenterCrop(), RoundedCorners(4))
                            .into(binding.imageViewQuote)

                        binding.imageViewQuote.visibility = View.VISIBLE
                    }
                    Constants.AttachmentType.VIDEO.type -> {

                        val uri = Utils.getFileUri(
                            binding.imageViewQuote.context,
                            quote.thumbnailUri,
                            Constants.NapoleonCacheDirectories.VIDEOS.folder
                        )

                        Glide.with(binding.imageViewQuote)
                            .load(uri)
                            .thumbnail(0.1f)
                            .transform(CenterCrop(), RoundedCorners(4))
                            .into(binding.imageViewQuote)

                        binding.imageViewQuote.visibility = View.VISIBLE
                    }
                    else -> {
                        binding.imageViewQuote.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun bindAttachmentTypeQuote(
        messageAndAttachment: MessageAndAttachment
    ) {
        val resourceId: Int? = when (getAttachmentType(messageAndAttachment, isFromInputPanel)) {
            Constants.AttachmentType.IMAGE.type -> {
                setText(resources.getString(R.string.text_photo_quote))
                R.drawable.ic_image
            }
            Constants.AttachmentType.AUDIO.type -> {
                setText(resources.getString(R.string.text_audio_quote))
                R.drawable.ic_headset
            }
            Constants.AttachmentType.VIDEO.type -> {
                setText(resources.getString(R.string.text_video_quote))
                R.drawable.ic_video
            }
            Constants.AttachmentType.DOCUMENT.type -> {
                setText(resources.getString(R.string.text_document_quote))
                R.drawable.ic_docs
            }
            Constants.AttachmentType.GIF.type, Constants.AttachmentType.GIF_NN.type -> {
                setText(resources.getString(R.string.text_gif))
                R.drawable.ic_gif
            }
            Constants.AttachmentType.LOCATION.type -> {
                setText(resources.getString(R.string.text_location))
                R.drawable.ic_location
            }
            else -> null
        }

        resourceId?.let {
            binding.imageViewTypeQuote.visibility = View.VISIBLE
            binding.imageViewTypeQuote.setImageResource(resourceId)
        } ?: run {
            binding.imageViewTypeQuote.visibility = View.GONE
        }
    }

    private fun setText(text: String) {
        binding.textViewMessageQuote.text = text
    }

    private fun getAttachmentType(
        messageAndAttachment: MessageAndAttachment,
        isFromInputPanel: Boolean
    ): String {
        var attachmentType = ""

        messageAndAttachment.quote?.let { quote ->
            attachmentType =
                if (messageAndAttachment.attachmentList.count() == 0 && isFromInputPanel)
                    ""
                else if (messageAndAttachment.attachmentList.count() > 0 && isFromInputPanel)
                    messageAndAttachment.attachmentList.first().type
                else
                    quote.attachmentType

        } ?: run {
            val firstAttachment = messageAndAttachment.getFirstAttachment()
            firstAttachment?.let { attachment ->
                attachmentType = attachment.type
            }
        }
        return attachmentType
    }
}
