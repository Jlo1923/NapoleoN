package com.naposystems.pepito.ui.conversation.adapter

import android.content.Context
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.appcompat.widget.AppCompatImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.naposystems.pepito.R
import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.entity.message.attachments.Attachment
import com.naposystems.pepito.utility.BlurTransformation
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.Utils
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@BindingAdapter("messageDate", "formatTime")
fun bindMessageDate(textView: TextView, timestamp: Int, format: Int) {
    /*try {
        val sdf = if (format == Constants.TimeFormat.EVERY_TWENTY_FOUR_HOURS.time) {
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        } else {
            SimpleDateFormat("dd/MM/yyyy hh:mm aa", Locale.getDefault())
        }
        val netDate = Date(timestamp.toLong() * 1000)
        textView.text = sdf.format(netDate)
        textView.visibility = View.VISIBLE
    } catch (e: Exception) {
        Timber.e("Error parsing date")
    }*/

    val context = textView.context

    try {
        val timeInit = TimeUnit.MINUTES.toSeconds(1) + timestamp
        val timeSevenMoreDays = TimeUnit.DAYS.toSeconds(7) + timestamp
        val timeActual = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dayNext = sdf.format(Date((timestamp.toLong() + TimeUnit.DAYS.toSeconds(1)) * 1000))
        val dayMessage = sdf.format(Date(timestamp.toLong() * 1000))
        val dayActual = sdf.format(Date(timeActual * 1000))

        when {
            timeInit > timeActual -> {
                textView.text = context.getString(R.string.text_now)
            }
            timeInit < timeActual && dayMessage == dayActual -> {
                val sdf = if (format == Constants.TimeFormat.EVERY_TWENTY_FOUR_HOURS.time) {
                    SimpleDateFormat("HH:mm", Locale.getDefault())
                } else {
                    SimpleDateFormat("hh:mm aa", Locale.getDefault())
                }
                textView.text = sdf.format(Date(timestamp.toLong() * 1000))
            }
            timeInit < timeActual && dayNext == dayActual -> {
                textView.text = context.getString(R.string.text_yesterday)
            }
            else -> {
                val sdf = if (timeSevenMoreDays > timeActual) {
                    SimpleDateFormat("EEEE", Locale.getDefault())
                } else {
                    if (format == Constants.TimeFormat.EVERY_TWENTY_FOUR_HOURS.time) {
                        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    } else {
                        SimpleDateFormat("dd/MM/yyyy hh:mm aa", Locale.getDefault())
                    }
                }
                textView.text = sdf.format(Date(timestamp.toLong() * 1000))
            }
        }

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

@BindingAdapter("imageAttachment")
fun bindImageAttachment(
    imageView: ImageView,
    @Nullable messageAndAttachmentParam: MessageAndAttachment?
) {
    try {
        messageAndAttachmentParam?.let { messageAndAttachment ->
            Timber.d("bindImageAttachment")

            val context = imageView.context
            messageAndAttachment.getFirstAttachment()?.let { attachment ->
                imageView.visibility = View.VISIBLE

                if (messageAndAttachment.message.isMine == Constants.IsMine.YES.value) {
                    loadAttachment(attachment, imageView, true)
                } else {
                    when (attachment.status) {
                        Constants.AttachmentStatus.DOWNLOAD_COMPLETE.status -> {
                            loadAttachment(attachment, imageView, false)
                        }
                        Constants.AttachmentStatus.NOT_DOWNLOADED.status,
                        Constants.AttachmentStatus.DOWNLOAD_CANCEL.status,
                        Constants.AttachmentStatus.DOWNLOAD_ERROR.status -> {
                            loadBlurAttachment(
                                attachment,
                                imageView,
                                context
                            )
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
    firstAttachment: Attachment,
    imageView: ImageView,
    context: Context?
) {
    Timber.d("loadBlurAttachment")
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
}

private fun loadAttachment(
    firstAttachment: Attachment,
    imageView: ImageView,
    isMine: Boolean
) {
    when (firstAttachment.type) {
        Constants.AttachmentType.IMAGE.type, Constants.AttachmentType.LOCATION.type -> {
            Glide.with(imageView)
                .load(firstAttachment)
                .transform(CenterCrop(), RoundedCorners(8))
                .into(imageView)
        }
        Constants.AttachmentType.VIDEO.type -> {
            val uri = if (isMine) {
                Utils.getFileUri(
                    imageView.context,
                    firstAttachment.uri,
                    Constants.NapoleonCacheDirectories.VIDEOS.folder
                )
            } else {
                Utils.getFileUri(
                    imageView.context,
                    "${firstAttachment.webId}.${firstAttachment.extension}",
                    Constants.NapoleonCacheDirectories.VIDEOS.folder
                )
            }
            Timber.d("uri: $uri")
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

@BindingAdapter("iconForState")
fun bindIconForState(
    imageButton: AppCompatImageButton,
    messageAndAttachment: MessageAndAttachment
) {
    val firstAttachment = messageAndAttachment.getFirstAttachment()

    firstAttachment?.let { attachment ->

        val drawableId = when (attachment.status) {
            Constants.AttachmentStatus.UPLOAD_CANCEL.status -> {
                imageButton.visibility = View.VISIBLE
                R.drawable.ic_file_upload_black
            }
            Constants.AttachmentStatus.DOWNLOAD_CANCEL.status,
            Constants.AttachmentStatus.DOWNLOAD_ERROR.status -> {
                imageButton.visibility = View.VISIBLE
                R.drawable.ic_file_download_black
            }
            Constants.AttachmentStatus.DOWNLOADING.status,
            Constants.AttachmentStatus.SENDING.status -> {
                imageButton.visibility = View.VISIBLE
                R.drawable.ic_close_black_24
            }
            Constants.AttachmentStatus.DOWNLOAD_COMPLETE.status,
            Constants.AttachmentStatus.SENT.status -> {
                if (attachment.type == Constants.AttachmentType.VIDEO.type) {
                    imageButton.visibility = View.VISIBLE
                    R.drawable.ic_play_arrow_black
                } else {
                    imageButton.visibility = View.GONE
                    R.drawable.ic_file_download_black
                }
            }
            else -> {
                imageButton.visibility = View.GONE
                R.drawable.ic_file_download_black
            }
        }

        imageButton.setImageResource(drawableId)
    }
}