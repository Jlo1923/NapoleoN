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
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.source.local.entity.MessageAttachmentRelation
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
    @Nullable contact: ContactEntity?
) {
    if (contact != null) {
        val context = textView.context
        val formattedNickname = context.getString(R.string.label_nickname, contact.nicknameFake)
        textView.text = formattedNickname
    }
}

@BindingAdapter("nameActionBar")
fun bindName(
    textView: TextView,
    @Nullable contact: ContactEntity?
) {
    if (contact != null) {
        textView.text = contact.displayNameFake
    }
}

@BindingAdapter("avatarActionBar")
fun bindAvatar(
    imageView: ImageView,
    @Nullable contact: ContactEntity?
) {
    if (contact != null) {
        val context = imageView.context

        val defaultAvatar = ContextCompat.getDrawable(context, R.drawable.ic_default_avatar)

        Glide.with(context)
            .load(contact.imageUrlFake)
            .apply(
                RequestOptions()
                    .priority(Priority.NORMAL)
                    .fitCenter()
            ).error(defaultAvatar)
            .circleCrop()
            .into(imageView)
    }
}

@BindingAdapter("countDown")
fun bindCountDown(
    textView: TextView,
    @Nullable messageAndAttachmentRelationParam: MessageAttachmentRelation?
) {
    val animationScaleUp: Animation by lazy {
        AnimationUtils.loadAnimation(textView.context, R.anim.scale_up)
    }

    messageAndAttachmentRelationParam?.let { messageAndAttachment ->
        if (messageAndAttachment.messageEntity.status == Constants.MessageStatus.READED.status) {
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
    @Nullable messageAndAttachmentRelationParam: MessageAttachmentRelation?
) {
    try {
        messageAndAttachmentRelationParam?.let { messageAndAttachment ->

            val context = imageView.context
            messageAndAttachment.getFirstAttachment()?.let { attachment ->
                Timber.d("bindImageAttachment")

                imageView.visibility = View.VISIBLE

                if (messageAndAttachment.messageEntity.isMine == Constants.IsMine.YES.value) {
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
    messageAndAttachmentRelation: MessageAttachmentRelation
) {

    if (messageAndAttachmentRelation.attachmentEntityList.isNotEmpty()) {
        val context = textView.context
        val firstAttachment = messageAndAttachmentRelation.attachmentEntityList[0]

        textView.text =
            context.getString(R.string.text_attachment_document_name, firstAttachment.extension)
    }
}

@BindingAdapter("attachmentDocumentIcon")
fun bindAttachmentDocumentIcon(
    imageView: ImageView,
    messageAndAttachmentRelation: MessageAttachmentRelation
) {

    if (messageAndAttachmentRelation.attachmentEntityList.isNotEmpty()) {
        val firstAttachment = messageAndAttachmentRelation.attachmentEntityList[0]

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
    messageAndAttachmentRelation: MessageAttachmentRelation
) {
    val firstAttachment = messageAndAttachmentRelation.getFirstAttachment()

    firstAttachment?.let { attachment ->

        Timber.d("bindIconForState: ${attachment.id}, ${attachment.status}")

        val drawableId = when (attachment.status) {
            Constants.AttachmentStatus.UPLOAD_CANCEL.status -> {
                if (messageAndAttachmentRelation.messageEntity.status == Constants.MessageStatus.SENT.status ||
                    messageAndAttachmentRelation.messageEntity.status == Constants.MessageStatus.READED.status ||
                    messageAndAttachmentRelation.messageEntity.status == Constants.MessageStatus.UNREAD.status
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
                if (messageAndAttachmentRelation.messageEntity.isMine == Constants.IsMine.YES.value) {
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
    messageAndAttachmentRelation: MessageAttachmentRelation
) {
    if (messageAndAttachmentRelation.attachmentEntityList[0].type == Constants.AttachmentType.AUDIO.type ||
        messageAndAttachmentRelation.attachmentEntityList[0].type == Constants.AttachmentType.IMAGE.type ||
        messageAndAttachmentRelation.attachmentEntityList[0].type == Constants.AttachmentType.VIDEO.type
    ) {
        if (messageAndAttachmentRelation.messageEntity.status == Constants.MessageStatus.READED.status) {
            imageView.visibility = View.VISIBLE
        } else {
            imageView.visibility = View.INVISIBLE
        }
    }

}


private fun loadBlurAttachment(
    firstAttachment: AttachmentEntity,
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
    firstAttachment: AttachmentEntity,
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

