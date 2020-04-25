package com.naposystems.pepito.ui.conversation.adapter

import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.naposystems.pepito.R
import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.Utils
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

@BindingAdapter("messageDate", "formatTime")
fun bindMessageDate(textView: TextView, timestamp: Int, format : Int) {
    try {
        val sdf = if(format == Constants.TimeFormat.EVERY_TWENTY_FOUR_HOURS.time) {
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        } else {
            SimpleDateFormat("dd/MM/yyyy hh:mm aa", Locale.getDefault())
        }
        val netDate = Date(timestamp.toLong() * 1000)
        textView.text = sdf.format(netDate)
        textView.visibility = View.VISIBLE
    } catch (e: Exception) {
        Timber.e("Error parsing date")
    }
}

@BindingAdapter("nicknameActionBar")
fun bindNickname(textView: TextView, @Nullable contact: Contact?) {
    if (contact != null) {
        val context = textView.context
        val formattedNickname = when {
            contact.nicknameFake.isNotEmpty() -> {
                context.getString(R.string.label_nickname, contact.nicknameFake)
            }
            else -> {
                context.getString(R.string.label_nickname, contact.nickname)
            }
        }
        textView.text = formattedNickname
    }
}

@BindingAdapter("nameActionBar")
fun bindName(textView: TextView, @Nullable contact: Contact?) {
    if (contact != null) {
        when {
            contact.displayNameFake.isNotEmpty() -> {
                textView.text = contact.displayNameFake
            }
            contact.displayName.isNotEmpty() -> {
                textView.text = contact.displayName
            }
        }
    }
}

@BindingAdapter("avatarActionBar")
fun bindAvatar(imageView: ImageView, @Nullable contact: Contact?) {
    if (contact != null) {
        val context = imageView.context

        val defaultAvatar = context.resources.getDrawable(
            R.drawable.ic_default_avatar,
            context.theme
        )

        @Suppress("IMPLICIT_CAST_TO_ANY")
        val loadImage = when {
            contact.imageUrlFake.isNotEmpty() -> {
                Utils.getFileUri(
                    context = context!!,
                    fileName = contact.imageUrlFake,
                    subFolder = Constants.NapoleonCacheDirectories.IMAGE_FAKE_CONTACT.folder
                )
            }
            contact.imageUrl.isNotEmpty() -> {
                contact.imageUrl
            }
            else -> {
                defaultAvatar
            }
        }

        Glide.with(context)
            .load(loadImage)
            .circleCrop()
            .into(imageView)
    }
}

@BindingAdapter("isFirstMyMessage")
fun bindIsFirstMyMessage(constraintLayout: ConstraintLayout, isFirst: Boolean) {
    val context = constraintLayout.context
    constraintLayout.background = if (isFirst) {
        context.getDrawable(R.drawable.bg_my_message)
    } else {
        context.getDrawable(R.drawable.bg_my_message_rounded)
    }
}

@BindingAdapter("isFirstIncomingMessage")
fun bindIsFirstIncomingMessage(constraintLayout: ConstraintLayout, isFirst: Boolean) {
    val context = constraintLayout.context
    constraintLayout.background = if (isFirst) {
        context.getDrawable(R.drawable.bg_incoming_message)
    } else {
        context.getDrawable(R.drawable.bg_incoming_message_rounded)
    }
}

@BindingAdapter("countDown")
fun bindCountDown(
    textView: TextView,
    @Nullable messageAndAttachmentParam: MessageAndAttachment?
) {
    val animationScaleUp: Animation by lazy {
        AnimationUtils.loadAnimation(textView.context, R.anim.scale_up)
    }

    messageAndAttachmentParam?.let { messageAndAttachment ->
        if(messageAndAttachment.message.status == Constants.MessageStatus.READED.status) {
            textView.startAnimation(animationScaleUp)
            textView.visibility = View.VISIBLE
        }else {
            textView.visibility = View.GONE
        }
    }
}

@BindingAdapter("imageAttachment")
fun bindImageAttachment(
    imageView: ImageView,
    @Nullable messageAndAttachmentParam: MessageAndAttachment?
) {

    try {
        messageAndAttachmentParam?.let { messageAndAttachment ->
            if (messageAndAttachment.attachmentList.isNotEmpty()) {
                imageView.visibility = View.VISIBLE
                val firstAttachment = messageAndAttachment.attachmentList[0]

                when (firstAttachment.type) {
                    Constants.AttachmentType.IMAGE.type, Constants.AttachmentType.LOCATION.type -> {
                        Glide.with(imageView)
                            .load(firstAttachment)
                            .transform(CenterCrop(), RoundedCorners(8))
                            .into(imageView)
                    }
                    Constants.AttachmentType.GIF.type, Constants.AttachmentType.GIF_NN.type -> {
                        Glide.with(imageView)
                            .asGif()
                            .load(firstAttachment)
                            .into(imageView)
                    }
                    Constants.AttachmentType.VIDEO.type -> {
                        val uri = Utils.getFileUri(
                            imageView.context,
                            firstAttachment.uri,
                            Constants.NapoleonCacheDirectories.VIDEOS.folder
                        )
                        Glide.with(imageView)
                            .load(uri)
                            .thumbnail(0.1f)
                            .transform(CenterCrop(), RoundedCorners(8))
                            .into(imageView)
                    }
                }
            }
        }
    } catch (e: Exception) {
        Timber.e(e)
    }
}

@BindingAdapter("attachmentDocumentName")
fun bindAttachmentDocumentName(textView: TextView, messageAndAttachment: MessageAndAttachment) {

    if (messageAndAttachment.attachmentList.isNotEmpty()) {
        val context = textView.context
        val firstAttachment = messageAndAttachment.attachmentList[0]

        textView.text =
            context.getString(R.string.text_attachment_document_name, firstAttachment.extension)
    }
}

@BindingAdapter("attachmentDocumentIcon")
fun bindAttachmentDocumentIcon(imageView: ImageView, messageAndAttachment: MessageAndAttachment) {

    if (messageAndAttachment.attachmentList.isNotEmpty()) {
        val firstAttachment = messageAndAttachment.attachmentList[0]

        val drawableId = when (firstAttachment.extension) {
            "doc" -> R.drawable.ic_attachment_doc
            "docx" -> R.drawable.ic_attachment_docx
            "xls" -> R.drawable.ic_attachment_xls
            "xlsx" -> R.drawable.ic_attachment_xlsx
            "ppt" -> R.drawable.ic_attachment_ppt
            "pptx" -> R.drawable.ic_attachment_pptx
            "pdf" -> R.drawable.ic_attachment_pdf
            else -> R.drawable.ic_document
        }

        Glide.with(imageView)
            .load(drawableId)
            .into(imageView)
    }
}