package com.naposystems.pepito.ui.home.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.naposystems.pepito.R
import com.naposystems.pepito.entity.message.Message
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.Utils
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@BindingAdapter("messageHour", "format", "colorText")
fun bindMessageDate(textView: TextView, timestamp: Int, format : Int, unreadMessages: Int) {
    textView.context?.let { context ->
        if(unreadMessages > 0) {
            textView.setTextColor(
                Utils.convertAttrToColorResource(context, R.attr.attrColorButtonTint)
            )
        } else {
            textView.setTextColor(
                Utils.convertAttrToColorResource(context, R.attr.attrTextColorMessageAndTimeHome)
            )
        }

        try {
            val timeInit = TimeUnit.MINUTES.toSeconds(3) + timestamp
            val timeSevenMoreDays = TimeUnit.DAYS.toSeconds(7) + timestamp
            val timeActual = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
            when{
                timeInit > timeActual -> {
                    textView.text = context.getString(R.string.text_now)
                }
                else -> {
                    val sdf = if(timeSevenMoreDays > timeActual) {
                        SimpleDateFormat("EEEE", Locale.getDefault())
                    } else {
                        if(format == Constants.TimeFormat.EVERY_TWENTY_FOUR_HOURS.time) {
                            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        } else {
                            SimpleDateFormat("dd/MM/yyyy hh:mm aa", Locale.getDefault())
                        }
                    }

                    val netDate = Date(timestamp.toLong() * 1000)

                    textView.text = sdf.format(netDate)
                }
            }

            textView.visibility = View.VISIBLE
        } catch (e: Exception) {
            Timber.e("Error parsing date")
        }
    }
}

@BindingAdapter("iconByConversation")
fun bindIconByConversation(imageView: ImageView, messageAndAttachment: MessageAndAttachment?) {
    if(messageAndAttachment?.attachmentList?.count()!! > 0){
        imageView.visibility = View.VISIBLE
        val resourceId: Int? = when (messageAndAttachment.attachmentList.last().type) {
            Constants.AttachmentType.IMAGE.type -> R.drawable.ic_image
            Constants.AttachmentType.AUDIO.type -> R.drawable.ic_headset
            Constants.AttachmentType.VIDEO.type -> R.drawable.ic_video
            Constants.AttachmentType.DOCUMENT.type -> R.drawable.ic_docs
            Constants.AttachmentType.GIF.type, Constants.AttachmentType.GIF_NN.type -> R.drawable.ic_gif
            Constants.AttachmentType.LOCATION.type -> R.drawable.ic_location
            else -> null
        }
        resourceId?.let { resource ->
            imageView.setImageResource(resource)
        }
    } else {
        imageView.visibility = View.GONE
    }
}

@BindingAdapter("bodyConversation")
fun bindBodyConversation(textView: TextView, messageAndAttachment: MessageAndAttachment?) {
    val context = textView.context
    if(messageAndAttachment?.message?.body?.count()!! > 0){
        textView.text = messageAndAttachment.message.body
    } else {
        if(messageAndAttachment.attachmentList.count() > 0){
            val stringId: Int? = when (messageAndAttachment.attachmentList.last().type) {
                Constants.AttachmentType.IMAGE.type -> R.string.text_photo
                Constants.AttachmentType.AUDIO.type -> R.string.text_audio
                Constants.AttachmentType.VIDEO.type -> R.string.text_video
                Constants.AttachmentType.DOCUMENT.type -> R.string.text_document
                Constants.AttachmentType.GIF.type, Constants.AttachmentType.GIF_NN.type -> R.string.text_gif
                Constants.AttachmentType.LOCATION.type -> R.string.text_location
                else -> null
            }
            stringId?.let { string ->
                textView.text = context.getString(string)
            }
        } else {
            textView.text = ""
        }
    }
}

@BindingAdapter("unreadMessages")
fun bindUnreadMessages(textView: TextView, unreadMessages: Int) {
    if (unreadMessages == 0) {
        textView.visibility = View.GONE
    } else {
        textView.visibility = View.VISIBLE
        textView.text = unreadMessages.toString()
    }
}

@BindingAdapter("statusMessage")
fun bindStatusMessage(imageView: ImageView, message: Message) {
    val context = imageView.context
    when(message.isMine) {
        Constants.IsMine.YES.value -> {
            imageView.visibility = View.VISIBLE
            val drawable = context.resources.getDrawable(drawableId(message.status), context.theme)
            imageView.setImageDrawable(drawable)
        } else -> {
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

private fun drawableId(status : Int) : Int {
    return when (status) {
        Constants.MessageStatus.SENDING.status -> R.drawable.ic_access_time_black
        Constants.MessageStatus.SENT.status -> R.drawable.ic_message_sent
        Constants.MessageStatus.UNREAD.status -> R.drawable.ic_message_unread
        Constants.MessageStatus.READED.status -> R.drawable.ic_message_readed
        Constants.MessageStatus.ERROR.status -> R.drawable.ic_error_outline_black
        else -> R.drawable.ic_access_time_black
    }
}