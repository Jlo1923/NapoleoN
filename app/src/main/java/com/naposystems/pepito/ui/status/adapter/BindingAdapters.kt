package com.naposystems.pepito.ui.status.adapter

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.naposystems.pepito.entity.Status

@BindingAdapter("status")
fun bindStatus(textView: TextView, status: Status) {
    val context = textView.context

    val newStatus: String

    newStatus = if (status.customStatus.isEmpty()) {
        context.getString(status.resourceId)
    } else {
        status.customStatus
    }

    textView.text = newStatus
}