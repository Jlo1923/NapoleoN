package com.naposystems.pepito.ui.home.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.naposystems.pepito.R
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.utility.Constants
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

@BindingAdapter("messageHour")
fun bindMessageDate(textView: TextView, timestamp: Int) {
    try {
        val sdf = SimpleDateFormat("hh:mm aa", Locale.getDefault())
        val netDate = Date(timestamp.toLong() * 1000)
        textView.text = sdf.format(netDate)
        textView.visibility = View.VISIBLE
    } catch (e: Exception) {
        Timber.e("Error parsing date")
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

@BindingAdapter("messageStatus")
fun bindMessageStatus(imageView: ImageView, status: Int) {

    val context = imageView.context

    val drawableId = when (status) {
        Constants.MessageStatus.SENDING.status -> R.drawable.ic_access_time_black
        Constants.MessageStatus.SENT.status -> R.drawable.ic_message_sent
        Constants.MessageStatus.UNREAD.status -> R.drawable.ic_message_unread
        Constants.MessageStatus.READED.status -> R.drawable.ic_message_readed
        Constants.MessageStatus.ERROR.status -> R.drawable.ic_error_outline_black
        else -> R.drawable.ic_access_time_black
    }

    val drawable = context.resources.getDrawable(drawableId, context.theme)

    imageView.setImageDrawable(drawable)
}