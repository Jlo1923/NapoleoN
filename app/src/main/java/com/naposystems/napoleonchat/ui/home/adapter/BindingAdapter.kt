package com.naposystems.napoleonchat.ui.home.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import androidx.databinding.BindingAdapter
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.source.local.entity.MessageEntity
import com.naposystems.napoleonchat.source.local.entity.MessageAttachmentRelation
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Utils
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@BindingAdapter("messageHour", "format", "colorText")
fun bindMessageDate(textView: TextView, timestamp: Int, format: Int, unreadMessages: Int) {
    textView.context?.let { context ->
        if (unreadMessages > 0) {
            textView.setTextColor(
                Utils.convertAttrToColorResource(context, R.attr.attrColorButtonTint)
            )
        } else {
            textView.setTextColor(
                Utils.convertAttrToColorResource(context, R.attr.attrTextColorMessageAndTimeHome)
            )
        }

        try {
            val timeInit = TimeUnit.MINUTES.toSeconds(2) + timestamp
            val timeSevenMoreDays = TimeUnit.DAYS.toSeconds(7) + timestamp
            val timeActual = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val dayNext = sdf.format(Date((timestamp.toLong() + TimeUnit.DAYS.toSeconds(1)) * 1000))
            val dayMessage = sdf.format(Date(timestamp.toLong() * 1000))
            val dayActual = sdf.format(Date(timeActual * 1000))

            when {
                timeInit > timeActual -> {
                    textView.text = returnHour(format).format(Date(timestamp.toLong() * 1000))
                }
                timeInit < timeActual && dayMessage == dayActual -> {
                    textView.text = returnHour(format).format(Date(timestamp.toLong() * 1000))
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
}

private fun returnHour(format: Int) : SimpleDateFormat {
    return if (format == Constants.TimeFormat.EVERY_TWENTY_FOUR_HOURS.time) {
        SimpleDateFormat("HH:mm", Locale.getDefault())
    } else {
        SimpleDateFormat("hh:mm aa", Locale.getDefault())
    }
}

@BindingAdapter("iconByConversation")
fun bindIconByConversation(imageView: ImageView, messageAndAttachmentRelation: MessageAttachmentRelation?) {
    var resourceId: Int?
    messageAndAttachmentRelation?.let { messageAndAttach ->
        resourceId = when (messageAndAttach.messageEntity.messageType) {
            Constants.MessageTextType.NORMAL.type -> {
                if (messageAndAttach.attachmentEntityList.count() > 0) {
                    when (messageAndAttach.attachmentEntityList.last().type) {
                        Constants.AttachmentType.IMAGE.type -> R.drawable.ic_image
                        Constants.AttachmentType.AUDIO.type -> R.drawable.ic_headset
                        Constants.AttachmentType.VIDEO.type -> R.drawable.ic_video
                        Constants.AttachmentType.DOCUMENT.type -> R.drawable.ic_docs
                        Constants.AttachmentType.GIF.type, Constants.AttachmentType.GIF_NN.type -> R.drawable.ic_gif
                        Constants.AttachmentType.LOCATION.type -> R.drawable.ic_location
                        else -> null
                    }
                } else {
                    null
                }
            }
            Constants.MessageTextType.MISSED_CALL.type -> R.drawable.ic_call_missed_red
            Constants.MessageTextType.MISSED_VIDEO_CALL.type -> R.drawable.ic_videocall_missed_red
            Constants.MessageTextType.NEW_CONTACT.type -> R.drawable.ic_people_tint
            else -> null
        }

        if (resourceId != null) {
            imageView.visibility = View.VISIBLE
            imageView.setImageResource(resourceId!!)
        } else {
            imageView.visibility = View.GONE
        }

    }
}

@BindingAdapter("bodyConversation")
fun bindBodyConversation(textView: TextView, messageAndAttachmentRelation: MessageAttachmentRelation?) {
    val context = textView.context
    var text = ""
    messageAndAttachmentRelation?.let { messageAndAttach ->
        text = when (messageAndAttach.messageEntity.messageType) {
            Constants.MessageTextType.NORMAL.type -> {
                if (messageAndAttach.messageEntity.body.count() > 0) {
                    messageAndAttach.messageEntity.body
                } else {
                    if (messageAndAttach.attachmentEntityList.count() > 0) {
                        val stringId: Int? = when (messageAndAttach.attachmentEntityList.last().type) {
                            Constants.AttachmentType.IMAGE.type -> R.string.text_photo
                            Constants.AttachmentType.AUDIO.type -> R.string.text_audio
                            Constants.AttachmentType.VIDEO.type -> R.string.text_video
                            Constants.AttachmentType.DOCUMENT.type -> R.string.text_document
                            Constants.AttachmentType.GIF.type, Constants.AttachmentType.GIF_NN.type -> R.string.text_gif
                            Constants.AttachmentType.LOCATION.type -> R.string.text_location
                            else -> null
                        }
                        stringId?.let { string ->
                            context.getString(string)
                        } ?: ""
                    } else {
                        ""
                    }
                }
            }
            Constants.MessageTextType.MISSED_CALL.type -> {
                context.getString(R.string.text_missed_voice_call)
            }
            Constants.MessageTextType.MISSED_VIDEO_CALL.type -> {
                context.getString(R.string.text_missed_video_call)
            }
            Constants.MessageTextType.NEW_CONTACT.type -> {
                context.getString(R.string.text_new_contact)
            }
            else -> ""
        }

        textView.text = text
        when (messageAndAttach.messageEntity.messageType) {
            Constants.MessageTextType.MISSED_CALL.type,
            Constants.MessageTextType.MISSED_VIDEO_CALL.type,
            Constants.MessageTextType.NEW_CONTACT.type -> {
                TextViewCompat.setTextAppearance(textView, R.style.italicText)
            }
            else -> {
                TextViewCompat.setTextAppearance(textView, R.style.normalText)
            }
        }
    }
}

@BindingAdapter("unreadMessages", "typeMessage")
fun bindUnreadMessages(textView: TextView, unreadMessages: Int, typeMessage : Int) {
    textView.visibility = View.VISIBLE
    when (unreadMessages) {
        0 -> {
            textView.visibility = View.GONE
        }
        in 1..99 -> {
            if (typeMessage == Constants.MessageTextType.NORMAL.type || typeMessage == Constants.MessageTextType.GROUP_DATE.type) {
                textView.visibility = View.VISIBLE
                textView.text = unreadMessages.toString()
            } else {
                textView.visibility = View.GONE
            }
        }
        else -> {
            val max = "+99"
            textView.text = max
        }
    }
}

@BindingAdapter("statusMessage")
fun bindStatusMessage(imageView: ImageView, messageEntity: MessageEntity) {
    val context = imageView.context
    when (messageEntity.isMine) {
        Constants.IsMine.YES.value -> {
            if (messageEntity.messageType != Constants.MessageTextType.NEW_CONTACT.type) {
                imageView.visibility = View.VISIBLE
                val drawable =
                    context.resources.getDrawable(drawableId(messageEntity.status), context.theme)
                imageView.setImageDrawable(drawable)
            } else {
                imageView.visibility = View.GONE
            }
        }
        else -> {
            imageView.visibility = View.GONE
        }
    }
}

@BindingAdapter("messageStatus")
fun bindMessageStatus(imageView: ImageView, status: Int) {
    val context = imageView.context
    val drawable = context.resources.getDrawable(drawableId(status), context.theme)
    imageView.setImageDrawable(drawable)
}

private fun drawableId(status: Int): Int {
    return when (status) {
        Constants.MessageStatus.SENDING.status -> R.drawable.ic_access_time_black
        Constants.MessageStatus.SENT.status -> R.drawable.ic_message_sent
        Constants.MessageStatus.UNREAD.status -> R.drawable.ic_message_unread
        Constants.MessageStatus.READED.status -> R.drawable.ic_message_readed
        Constants.MessageStatus.ERROR.status -> R.drawable.ic_message_error
        else -> R.drawable.ic_access_time_black
    }
}