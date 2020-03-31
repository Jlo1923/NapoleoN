package com.naposystems.pepito.ui.custom.inputPanel.adapters

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.naposystems.pepito.R
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.entity.message.attachments.Attachment
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.Utils

@BindingAdapter("userBackground", "isFromInputPanelQuote")
fun bindUserBackground(
    constraintLayout: ConstraintLayout,
    @Nullable messageAndAttachmentNull: MessageAndAttachment?,
    isFromInputPanel: Boolean
) {
    val context = constraintLayout.context
    val quoteNull = messageAndAttachmentNull?.quote

    val test = context.getDrawable(R.drawable.bg_my_quote)

//    test.

    if(isFromInputPanel){
        messageAndAttachmentNull?.message?.let {message ->
            constraintLayout.background = if (message.isMine == Constants.IsMine.YES.value) {
                context.getDrawable(R.drawable.bg_your_quote)
            } else {
                context.getDrawable(R.drawable.bg_my_quote)
            }
        }
    } else {
        quoteNull?.let { quote ->
            constraintLayout.background = if (quote.isMine == Constants.IsMine.YES.value) {
                context.getDrawable(R.drawable.bg_your_quote)
            } else {
                context.getDrawable(R.drawable.bg_my_quote)
            }
        }
    }
}

@BindingAdapter("userQuote", "isFromInputPanelQuote")
fun bindUserQuote(
    textView: TextView,
    @Nullable messageAndAttachmentNull: MessageAndAttachment?,
    isFromInputPanel: Boolean
) {
    var isMineNull: Int? = null

    messageAndAttachmentNull?.quote?.let { quote ->
        isMineNull = if (isFromInputPanel) {
            messageAndAttachmentNull.message.isMine
        } else {
            quote.isMine
        }
    } ?: run {
        messageAndAttachmentNull?.message.let { message ->
            isMineNull = message?.isMine
        }
    }

    if (isMineNull == Constants.IsMine.YES.value) {
        textView.setTextColor(Color.parseColor("#29ABE2"))
        textView.text = "Tu|!!"
    } else {
        val contact = messageAndAttachmentNull?.contact
        textView.setTextColor(Color.parseColor("#DA5E58"))
        textView.text = contact?.let {
            if (contact.nicknameFake.isNotEmpty()) {
                contact.nicknameFake
            } else {
                contact.nickname
            }
        } ?: run {
            ""
        }
    }
}

@BindingAdapter("bodyQuote", "isFromInputPanelQuote")
fun bindBodyQuote(
    textView: TextView,
    @Nullable messageAndAttachment: MessageAndAttachment?,
    isFromInputPanel: Boolean
) {

    val body = if (isFromInputPanel) {
        val messageNull = messageAndAttachment?.message

        messageNull?.body ?: ""
    } else {
        messageAndAttachment?.quote?.body ?: ""
    }

    textView.text = if (body.isNotEmpty()) {
        body
    } else {
        when (getAttachmentType(messageAndAttachment, isFromInputPanel)) {
            Constants.AttachmentType.IMAGE.type -> "Foto|!!"
            Constants.AttachmentType.AUDIO.type -> "Audio|!!"
            Constants.AttachmentType.VIDEO.type -> "Video|!!"
            Constants.AttachmentType.DOCUMENT.type -> "Documento|!!"
            else -> ""
        }
    }
}

@BindingAdapter("imageQuote", "isFromInputPanelQuote")
fun bindImageQuote(
    imageView: ImageView,
    @Nullable messageAndAttachmentNull: MessageAndAttachment?,
    isFromInputPanel: Boolean
) {
    var firstAttachmentNull: Attachment?

    messageAndAttachmentNull?.let { messageAndAttachment ->
        if (isFromInputPanel) {
            firstAttachmentNull = MessageAndAttachment.getFirstAttachment(messageAndAttachment)

            firstAttachmentNull?.let { attachment ->
                if (attachment.type == Constants.AttachmentType.IMAGE.type) {
                    Glide.with(imageView)
                        .load(attachment)
                        .transform(CenterCrop(), RoundedCorners(4))
                        .into(imageView)
                } else if (attachment.type == Constants.AttachmentType.VIDEO.type) {
                    val uri = Utils.getFileUri(
                        imageView.context,
                        attachment.uri,
                        Constants.NapoleonCacheDirectories.VIDEOS.folder
                    )
                    Glide.with(imageView)
                        .load(uri)
                        .thumbnail(0.1f)
                        .transform(CenterCrop(), RoundedCorners(4))
                        .into(imageView)
                }
                imageView.visibility = View.VISIBLE
            } ?: run {
                imageView.visibility = View.GONE
            }
        } else {
            messageAndAttachment.quote?.let { quote ->

                when (quote.attachmentType) {
                    Constants.AttachmentType.IMAGE.type -> {

                        val uri = Utils.getFileUri(
                            imageView.context,
                            quote.thumbnailUri,
                            Constants.NapoleonCacheDirectories.IMAGES.folder
                        )

                        Glide.with(imageView)
                            .load(uri)
                            .transform(CenterCrop(), RoundedCorners(4))
                            .into(imageView)

                        imageView.visibility = View.VISIBLE
                    }
                    Constants.AttachmentType.VIDEO.type -> {

                        val uri = Utils.getFileUri(
                            imageView.context,
                            quote.thumbnailUri,
                            Constants.NapoleonCacheDirectories.VIDEOS.folder
                        )

                        Glide.with(imageView)
                            .load(uri)
                            .thumbnail(0.1f)
                            .transform(CenterCrop(), RoundedCorners(4))
                            .into(imageView)

                        imageView.visibility = View.VISIBLE
                    }
                    else -> {
                        imageView.visibility = View.GONE
                    }
                }
            }
        }
    } ?: run {
        imageView.visibility = View.GONE
    }
}

@BindingAdapter("attachmentTypeQuote", "isFromInputPanelQuote")
fun bindAttachmentTypeQuote(
    imageView: ImageView,
    @Nullable messageAndAttachmentNull: MessageAndAttachment?,
    isFromInputPanel: Boolean
) {
    val attachmentType = getAttachmentType(messageAndAttachmentNull, isFromInputPanel)

    attachmentType?.let {
        val resourceId: Int? = when (attachmentType) {
            Constants.AttachmentType.IMAGE.type -> R.drawable.ic_image_quote_black
            Constants.AttachmentType.AUDIO.type -> R.drawable.ic_headset_quote_black
            Constants.AttachmentType.VIDEO.type -> R.drawable.ic_video_quote_black
            Constants.AttachmentType.DOCUMENT.type -> R.drawable.ic_document_quote_black
            else -> null
        }

        resourceId?.let {
            imageView.visibility = View.VISIBLE
            imageView.setImageResource(resourceId)
        } ?: run {
            imageView.visibility = View.GONE
        }
    } ?: run {
        imageView.visibility = View.GONE
    }
}

private fun getAttachmentType(
    messageAndAttachmentNull: MessageAndAttachment?,
    isFromInputPanel: Boolean
): String? {
    var attachmentType: String? = ""

    messageAndAttachmentNull?.let { messageAndAttachment ->
        messageAndAttachment.quote?.let { quote ->
            attachmentType =
                if (messageAndAttachment.attachmentList.count() == 0 && isFromInputPanel)
                    ""
                else if (messageAndAttachment.attachmentList.count() > 0 && isFromInputPanel)
                    messageAndAttachment.attachmentList.first().type
                else
                    quote.attachmentType

        } ?: run {
            val firstAttachment = MessageAndAttachment.getFirstAttachment(messageAndAttachment)
            firstAttachment?.let { attachment ->
                attachmentType = attachment.type
            }
        }
    }
    return attachmentType
}