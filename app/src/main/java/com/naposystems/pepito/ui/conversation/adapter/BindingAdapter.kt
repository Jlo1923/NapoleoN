package com.naposystems.pepito.ui.conversation.adapter

import android.content.Context
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
import com.naposystems.pepito.entity.message.Message
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.entity.message.attachments.Attachment
import com.naposystems.pepito.utility.BlurTransformation
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.Utils
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

@BindingAdapter("messageDate")
fun bindMessageDate(textView: TextView, timestamp: Int) {
    try {
        val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm aa", Locale.getDefault())
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
        if (messageAndAttachment.message.status == Constants.MessageStatus.READED.status) {
            textView.startAnimation(animationScaleUp)
            textView.visibility = View.VISIBLE
        } else {
            textView.visibility = View.GONE
        }
    }
}

@BindingAdapter("imageAttachment", "clickListener", "itemPosition")
fun bindImageAttachment(
    imageView: ImageView,
    @Nullable messageAndAttachmentParam: MessageAndAttachment?,
    @Nullable clickListener: ConversationAdapter.ClickListener?,
    @Nullable itemPosition: Int?
) {
    try {
        messageAndAttachmentParam?.let { messageAndAttachment ->
            val context = imageView.context
            val message = messageAndAttachment.message

            if (messageAndAttachment.attachmentList.isNotEmpty()) {
                imageView.visibility = View.VISIBLE
                val firstAttachment = messageAndAttachment.attachmentList[0]

                if (messageAndAttachment.message.isMine == Constants.IsMine.YES.value) {
                    loadAttachment(firstAttachment, imageView)
                } else {
                    when (firstAttachment.status) {
                        Constants.AttachmentStatus.NOT_DOWNLOADED.status -> {
                            loadBlurAttachment(
                                message,
                                firstAttachment,
                                imageView,
                                context,
                                clickListener,
                                itemPosition
                            )
                        }
                        Constants.AttachmentStatus.DOWNLOAD_COMPLETE.status -> {
                            loadAttachment(firstAttachment, imageView)
                        }
                    }
                }
            }
        }
    } catch (e: Exception) {
        Timber.e(e)
    }
}

private fun loadBlurAttachment(
    message: Message,
    firstAttachment: Attachment,
    imageView: ImageView,
    context: Context?,
    clickListener: ConversationAdapter.ClickListener?,
    itemPosition: Int?
) {
    if (message.status == Constants.MessageStatus.READED.status) {
        val fileName = "${firstAttachment.webId}.${firstAttachment.extension}"
        firstAttachment.status = Constants.AttachmentStatus.DOWNLOADING.status
        firstAttachment.uri = fileName
        when (firstAttachment.type) {
            Constants.AttachmentType.IMAGE.type, Constants.AttachmentType.LOCATION.type -> {
                Glide.with(imageView)
                    .load(firstAttachment.body)
                    .transform(
                        CenterCrop(),
                        RoundedCorners(8),
                        BlurTransformation(context)
                    )
                    .into(imageView)
            }
            Constants.AttachmentType.VIDEO.type,
            Constants.AttachmentType.GIF.type,
            Constants.AttachmentType.GIF_NN.type -> {
                Glide.with(imageView)
                    .load(firstAttachment.body)
                    .thumbnail(0.1f)
                    .transform(
                        CenterCrop(),
                        RoundedCorners(8),
                        BlurTransformation(context)
                    )
                    .into(imageView)
            }
        }
        clickListener?.downloadAttachment(firstAttachment, itemPosition)
    }
}

private fun loadAttachment(
    firstAttachment: Attachment,
    imageView: ImageView
) {
    when (firstAttachment.type) {
        Constants.AttachmentType.IMAGE.type, Constants.AttachmentType.LOCATION.type -> {
            Glide.with(imageView)
                .load(firstAttachment)
                .transform(CenterCrop(), RoundedCorners(8))
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
        Constants.AttachmentType.GIF.type, Constants.AttachmentType.GIF_NN.type -> {
            Glide.with(imageView)
                .asGif()
                .load(firstAttachment)
                .into(imageView)
        }
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