package com.naposystems.pepito.ui.home.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.naposystems.pepito.R
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
        Timber.d("Error parsing date")
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

    val drawable = when (status) {
        Constants.MessageStatus.SENT.status -> context.resources.getDrawable(
            R.drawable.ic_message_sent,
            context.theme
        )
        Constants.MessageStatus.UNREAD.status -> context.resources.getDrawable(
            R.drawable.ic_message_unread,
            context.theme
        )
        Constants.MessageStatus.READED.status -> context.resources.getDrawable(
            R.drawable.ic_message_readed,
            context.theme
        )
        else ->
            context.resources.getDrawable(
                R.drawable.ic_message_sent,
                context.theme
            )
    }

    imageView.setImageDrawable(drawable)
}