package com.naposystems.napoleonchat.ui.conversation.adapter

import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.appcompat.widget.AppCompatImageButton
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.entity.Contact
import com.naposystems.napoleonchat.entity.message.MessageAndAttachment
import com.naposystems.napoleonchat.entity.message.attachments.Attachment
import com.naposystems.napoleonchat.utility.BlurTransformation
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Utils
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@BindingAdapter("messageDateSend", "formatTime")
fun bindMessageDateSend(
    textView: TextView,
    timestamp: Int,
    format: Int
) {
    try {
        val timeActual = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
        val sdfActual = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dayMessage = sdfActual.format(Date(timestamp.toLong() * 1000))
        val dayActual = sdfActual.format(Date(timeActual * 1000))

        val sdf = when (dayMessage) {
            dayActual -> {
                if (format == Constants.TimeFormat.EVERY_TWENTY_FOUR_HOURS.time) {
                    SimpleDateFormat("HH:mm", Locale.getDefault())
                } else {
                    SimpleDateFormat("hh:mm aa", Locale.getDefault())
                }
            }
            else -> {
                if (format == Constants.TimeFormat.EVERY_TWENTY_FOUR_HOURS.time) {
                    SimpleDateFormat("dd/MM/yy   HH:mm", Locale.getDefault())
                } else {
                    SimpleDateFormat("dd/MM/yy   hh:mm aa", Locale.getDefault())
                }
            }
        }
        textView.setTypeface(textView.typeface, Typeface.ITALIC)
        val text = "${sdf.format(Date(timestamp.toLong() * 1000))} "
        textView.text = text
        textView.visibility = View.VISIBLE
    } catch (e: Exception) {
        Timber.e("Error parsing date")
    }
}

@BindingAdapter("messageDateIncoming", "formatTime")
fun bindMessageDateIncoming(
    textView: TextView,
    timestamp: Int,
    format: Int
) {
    try {
        val timeActual = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
        val sdfActual = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dayMessage = sdfActual.format(Date(timestamp.toLong() * 1000))
        val dayActual = sdfActual.format(Date(timeActual * 1000))

        val sdf = when (dayMessage) {
            dayActual -> {
                if (format == Constants.TimeFormat.EVERY_TWENTY_FOUR_HOURS.time) {
                    SimpleDateFormat("HH:mm", Locale.getDefault())
                } else {
                    SimpleDateFormat("hh:mm aa", Locale.getDefault())
                }
            }
            else -> {
                if (format == Constants.TimeFormat.EVERY_TWENTY_FOUR_HOURS.time) {
                    SimpleDateFormat("dd/MM/yy   HH:mm ", Locale.getDefault())
                } else {
                    SimpleDateFormat("dd/MM/yy   hh:mm aa ", Locale.getDefault())
                }
            }
        }
        val text = "${sdf.format(Date(timestamp.toLong() * 1000))} "
        textView.text = text
        textView.visibility = View.VISIBLE
    } catch (e: Exception) {
        Timber.e("Error parsing date")
    }
}

@BindingAdapter("nicknameActionBar")
fun bindNickname(
    textView: TextView,
    @Nullable contact: Contact?
) {
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
fun bindName(
    textView: TextView,
    @Nullable contact: Contact?
) {
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
fun bindAvatar(
    imageView: ImageView,
    @Nullable contact: Contact?
) {
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
                    subFolder = Constants.CacheDirectories.IMAGE_FAKE_CONTACT.folder
                )
            }
            contact.imageUrl.isNotEmpty() -> {
                contact.imageUrl
            }
            else -> {
                ""
            }
        }

        if (loadImage != "") {
            Glide.with(context)
                .load(loadImage)
                .circleCrop()
                .into(imageView)
        } else {
            imageView.setImageDrawable(defaultAvatar)
        }

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

            val context = imageView.context
            messageAndAttachment.getFirstAttachment()?.let { attachment ->
                Timber.d("bindImageAttachment")

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
                        Constants.AttachmentStatus.DOWNLOAD_ERROR.status,
                        Constants.AttachmentStatus.DOWNLOADING.status -> {
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

@BindingAdapter("attachmentDocumentName")
fun bindAttachmentDocumentName(
    textView: TextView,
    messageAndAttachment: MessageAndAttachment
) {

    if (messageAndAttachment.attachmentList.isNotEmpty()) {
        val context = textView.context
        val firstAttachment = messageAndAttachment.attachmentList[0]

        textView.text =
            context.getString(R.string.text_attachment_document_name, firstAttachment.extension)
    }
}

@BindingAdapter("attachmentDocumentIcon")
fun bindAttachmentDocumentIcon(
    imageView: ImageView,
    messageAndAttachment: MessageAndAttachment
) {

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

        Timber.d("bindIconForState: ${attachment.id}, ${attachment.status}")

        val drawableId = when (attachment.status) {
            Constants.AttachmentStatus.UPLOAD_CANCEL.status -> {
                if (messageAndAttachment.message.status == Constants.MessageStatus.SENT.status ||
                    messageAndAttachment.message.status == Constants.MessageStatus.READED.status ||
                    messageAndAttachment.message.status == Constants.MessageStatus.UNREAD.status
                ) {
                    imageButton.visibility = View.GONE
                } else {
                    imageButton.visibility = View.VISIBLE
                }
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
            Constants.AttachmentStatus.DOWNLOAD_COMPLETE.status -> {
                if (attachment.type == Constants.AttachmentType.VIDEO.type) {
                    imageButton.visibility = View.GONE
                    R.drawable.ic_play_arrow_black
                } else {
                    imageButton.visibility = View.GONE
                    R.drawable.ic_file_download_black
                }
            }
            Constants.AttachmentStatus.SENT.status -> {
                imageButton.visibility = View.GONE
                R.drawable.ic_file_upload_black
            }
            Constants.AttachmentStatus.ERROR.status -> {
                imageButton.visibility = View.VISIBLE
                if (messageAndAttachment.message.isMine == Constants.IsMine.YES.value) {
                    R.drawable.ic_file_upload_black
                } else {
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

@BindingAdapter("showCheck")
fun bindShowCheck(
    imageView: ImageView,
    messageAndAttachment: MessageAndAttachment
) {
    if (messageAndAttachment.attachmentList[0].type == Constants.AttachmentType.AUDIO.type ||
        messageAndAttachment.attachmentList[0].type == Constants.AttachmentType.IMAGE.type ||
        messageAndAttachment.attachmentList[0].type == Constants.AttachmentType.VIDEO.type
    ) {
        if (messageAndAttachment.message.status == Constants.MessageStatus.READED.status) {
            imageView.visibility = View.VISIBLE
        } else {
            imageView.visibility = View.INVISIBLE
        }
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
        Constants.AttachmentType.LOCATION.type -> {
            Glide.with(imageView)
                .load(firstAttachment)
                .transform(RoundedCorners(8))
                .into(imageView)
        }
        Constants.AttachmentType.IMAGE.type -> {
            Glide.with(imageView)
                .load(firstAttachment)
                .transform(
                    CenterCrop(),
                    BlurTransformation(imageView.context)
                )
                .into(imageView)
        }
        Constants.AttachmentType.VIDEO.type -> {
            val uri = if (isMine) {
                Utils.getFileUri(
                    imageView.context,
                    firstAttachment.fileName,
                    Constants.CacheDirectories.VIDEOS.folder
                )
            } else {
                Utils.getFileUri(
                    imageView.context,
                    "${firstAttachment.webId}.${firstAttachment.extension}",
                    Constants.CacheDirectories.VIDEOS.folder
                )
            }
            Glide.with(imageView)
                .load(uri)
                .thumbnail(0.1f)
                .transform(
                    CenterCrop(),
                    BlurTransformation(imageView.context)
                )
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
