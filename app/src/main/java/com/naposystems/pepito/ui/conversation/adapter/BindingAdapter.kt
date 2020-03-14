package com.naposystems.pepito.ui.conversation.adapter

import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.naposystems.pepito.R
import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.FileManager
import com.naposystems.pepito.utility.Utils
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
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
                contact.imageUrlFake
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

@BindingAdapter("audioDuration")
fun bindAudioDuration(textView: TextView, messageAndAttachment: MessageAndAttachment) {

    if (messageAndAttachment.attachmentList.isNotEmpty()) {
        val firstAudio = messageAndAttachment.attachmentList.first()
        val context = textView.context

        if (messageAndAttachment.message.isMine == Constants.IsMine.YES.value) {
            val fileDescriptor = context.contentResolver
                .openFileDescriptor(Uri.parse(firstAudio.uri), "r")

            val fileInputStream = FileInputStream(fileDescriptor!!.fileDescriptor)
        }
    } else {
        textView.visibility = View.GONE
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

@BindingAdapter("imageAttachment")
fun bindImageAttachment(imageView: ImageView, messageAndAttachment: MessageAndAttachment) {

    try {
        if (messageAndAttachment.attachmentList.isNotEmpty()) {
            imageView.visibility = View.VISIBLE
            val firstAttachment = messageAndAttachment.attachmentList[0]

            if (firstAttachment.type == Constants.AttachmentType.IMAGE.type) {
                Glide.with(imageView)
                    .load(firstAttachment)
                    .into(imageView)
            } else if (firstAttachment.type == Constants.AttachmentType.VIDEO.type) {
                val uri = Utils.getFileUri(
                    imageView.context,
                    firstAttachment.uri,
                    Constants.NapoleonCacheDirectories.VIDEOS.folder
                )
                Glide.with(imageView)
                    .load(uri)
                    .thumbnail(0.1f)
                    .into(imageView)
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
            else -> R.drawable.ic_attachment_document
        }

        Glide.with(imageView)
            .load(drawableId)
            .into(imageView)
    }
}