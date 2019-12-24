package com.naposystems.pepito.ui.conversation.adapter

import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

@BindingAdapter("messageDate")
fun bindMessageDate(textView: TextView, timestamp: String) {
    try {
        if (timestamp.isNotEmpty()) {
            val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm aa", Locale.getDefault())
            val netDate = Date(timestamp.toLong() * 1000)
            textView.text = sdf.format(netDate)
            textView.visibility = View.VISIBLE
        }
    } catch (e: Exception) {
        Timber.d("Error parsing date")
    }
}