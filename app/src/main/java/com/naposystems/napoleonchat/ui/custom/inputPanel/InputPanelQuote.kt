package com.naposystems.napoleonchat.ui.custom.inputPanel

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.CustomInputPanelQuoteBinding
import com.naposystems.napoleonchat.source.local.entity.MessageAttachmentRelation
import com.naposystems.napoleonchat.utility.BlurTransformation
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Utils

class InputPanelQuote(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs),
    IContractInputPanelQuote {

    private var binding: CustomInputPanelQuoteBinding
    private var isCancelable: Boolean = false
    private var isFromInputPanel: Boolean = false
    private var messageAndAttachmentRelation: MessageAttachmentRelation? = null

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

    override fun setupMessageAndAttachment(messageAndAttachmentRelation: MessageAttachmentRelation) {
        with(messageAndAttachmentRelation) {
            this@InputPanelQuote.messageAndAttachmentRelation = this
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
        this.messageAndAttachmentRelation = null
    }

    override fun resetImage() {
        binding.imageViewQuote.apply {
            this.setImageBitmap(null)
            this.visibility = View.GONE
        }
    }

    override fun getMessageAndAttachment() = this.messageAndAttachmentRelation

    private fun bindUserBackground(messageAndAttachmentRelation: MessageAttachmentRelation) {
        val quoteNull = messageAndAttachmentRelation.quoteEntity

        if (isFromInputPanel) {
            messageAndAttachmentRelation.messageEntity.let { message ->
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
                            && messageAndAttachmentRelation.messageEntity.isMine == 1 -> {
                        context.getDrawable(R.drawable.bg_my_quote_my_message)
                    }
                    quote.isMine == Constants.IsMine.YES.value
                            && messageAndAttachmentRelation.messageEntity.isMine == 0 -> {
                        context.getDrawable(R.drawable.bg_my_quote_incoming_message)
                    }
                    quote.isMine == Constants.IsMine.NO.value
                            && messageAndAttachmentRelation.messageEntity.isMine == 1 -> {
                        context.getDrawable(R.drawable.bg_your_quote_my_message)
                    }
                    quote.isMine == Constants.IsMine.NO.value
                            && messageAndAttachmentRelation.messageEntity.isMine == 0 -> {
                        context.getDrawable(R.drawable.bg_your_quote_incoming_message)
                    }
                    else -> {
                        context.getDrawable(R.drawable.bg_my_quote_my_message)
                    }
                }

                binding.textViewMessageQuote.setTextColor(
                    when {
                        quote.isMine == Constants.IsMine.YES.value
                                && messageAndAttachmentRelation.messageEntity.isMine == 1 -> {
                            Utils.convertAttrToColorResource(
                                context,
                                R.attr.attrTextColorBodyMyQuote
                            )
                        }
                        quote.isMine == Constants.IsMine.YES.value
                                && messageAndAttachmentRelation.messageEntity.isMine == 0 -> {
                            Utils.convertAttrToColorResource(
                                context,
                                R.attr.attrTextColorBodyMyQuote
                            )
                        }
                        quote.isMine == Constants.IsMine.NO.value
                                && messageAndAttachmentRelation.messageEntity.isMine == 1 -> {
                            Utils.convertAttrToColorResource(
                                context,
                                R.attr.attrTextColorBodyYourQuote
                            )
                        }
                        quote.isMine == Constants.IsMine.NO.value
                                && messageAndAttachmentRelation.messageEntity.isMine == 0 -> {
                            Utils.convertAttrToColorResource(
                                context,
                                R.attr.attrTextColorBodyYourQuote
                            )
                        }
                        else -> {
                            Utils.convertAttrToColorResource(
                                context,
                                R.attr.attrTextColorBodyMyQuote
                            )
                        }
                    }
                )
            }
        }
    }

    private fun bindUserQuote(messageAndAttachmentRelation: MessageAttachmentRelation) {
        var isMineNull: Int? = null

        messageAndAttachmentRelation.quoteEntity?.let { quote ->
            isMineNull = if (isFromInputPanel) {
                messageAndAttachmentRelation.messageEntity.isMine
            } else {
                quote.isMine
            }
        } ?: run {
            messageAndAttachmentRelation.messageEntity.let { message ->
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
            val contact = messageAndAttachmentRelation.contact
            binding.textViewTitleQuote.setTextColor(textColorYourName)
            binding.textViewTitleQuote.text = contact?.let {
                if (contact.nicknameFake.isNotEmpty()) {
                    contact.nicknameFake
                } else {
                    contact.nickname
                }
            }
        }
    }

    private fun bindBodyQuote(
        messageAndAttachmentRelation: MessageAttachmentRelation
    ) {
        val context = binding.textViewMessageQuote.context

        val body = if (isFromInputPanel) {
            val messageNull = messageAndAttachmentRelation.messageEntity

            messageNull.body
        } else {

            val quoteBody = messageAndAttachmentRelation.quoteEntity?.body ?: ""

            if (quoteBody.isNotEmpty()) {
                quoteBody
            } else {
                ""
            }
        }

        if (body.isNotEmpty()) {
            binding.textViewMessageQuote.text = body
        }
    }

    private fun bindImageQuote(
        messageAndAttachmentRelation: MessageAttachmentRelation
    ) {
        val transformationList: MutableList<Transformation<Bitmap>> = arrayListOf()

        transformationList.add(CenterCrop())
        transformationList.add(BlurTransformation(context))
        transformationList.add(RoundedCorners(4))

        if (isFromInputPanel) {
            val firstAttachmentNull = messageAndAttachmentRelation.getFirstAttachment()

            firstAttachmentNull?.let { attachment ->


                if (attachment.type == Constants.AttachmentType.IMAGE.type) {
                    Glide.with(binding.imageViewQuote)
                        .load(attachment)
                        .thumbnail(0.1f)
                        .transform(
                            *transformationList.toTypedArray()
                        )
                        .into(binding.imageViewQuote)
                } else if (attachment.type == Constants.AttachmentType.VIDEO.type) {
                    val uri = Utils.getFileUri(
                        binding.imageViewQuote.context,
                        attachment.fileName,
                        Constants.CacheDirectories.VIDEOS.folder
                    )
                    Glide.with(binding.imageViewQuote)
                        .load(uri)
                        .thumbnail(0.1f)
                        .transform(
                            *transformationList.toTypedArray()
                        )
                        .into(binding.imageViewQuote)
                }
                binding.imageViewQuote.visibility = View.VISIBLE
            } ?: run {
                binding.imageViewQuote.visibility = View.GONE
            }
        } else {
            messageAndAttachmentRelation.quoteEntity?.let { quote ->

                when (quote.attachmentType) {
                    Constants.AttachmentType.IMAGE.type -> {

                        val uri = Utils.getFileUri(
                            binding.imageViewQuote.context,
                            quote.thumbnailUri,
                            Constants.CacheDirectories.IMAGES.folder
                        )

                        Glide.with(binding.imageViewQuote)
                            .load(uri)
                            .transform(
                                *transformationList.toTypedArray()
                            )
                            .into(binding.imageViewQuote)

                        binding.imageViewQuote.visibility = View.VISIBLE
                    }
                    Constants.AttachmentType.VIDEO.type -> {

                        val uri = Utils.getFileUri(
                            binding.imageViewQuote.context,
                            quote.thumbnailUri,
                            Constants.CacheDirectories.VIDEOS.folder
                        )

                        Glide.with(binding.imageViewQuote)
                            .load(uri)
                            .thumbnail(0.1f)
                            .transform(
                                *transformationList.toTypedArray()
                            )
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
        messageAndAttachmentRelation: MessageAttachmentRelation
    ) {
        val resourceId: Int? = when (getAttachmentType(messageAndAttachmentRelation, isFromInputPanel)) {
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
        messageAndAttachmentRelation: MessageAttachmentRelation,
        isFromInputPanel: Boolean
    ): String {
        var attachmentType = ""

        messageAndAttachmentRelation.quoteEntity?.let { quote ->
            attachmentType =
                if (messageAndAttachmentRelation.attachmentEntityList.count() == 0 && isFromInputPanel)
                    ""
                else if (messageAndAttachmentRelation.attachmentEntityList.count() > 0 && isFromInputPanel)
                    messageAndAttachmentRelation.attachmentEntityList.first().type
                else
                    quote.attachmentType

        } ?: run {
            val firstAttachment = messageAndAttachmentRelation.getFirstAttachment()
            firstAttachment?.let { attachment ->
                attachmentType = attachment.type
            }
        }
        return attachmentType
    }
}
