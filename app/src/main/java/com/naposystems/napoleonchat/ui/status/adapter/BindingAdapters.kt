package com.naposystems.napoleonchat.ui.status.adapter

import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.databinding.BindingAdapter
import com.naposystems.napoleonchat.entity.Status

@BindingAdapter("status")
fun bindStatus(textView: TextView, status: Status) {
    val newStatus = if (status.customStatus.isEmpty() && status.status.isNotEmpty()) {
        status.status
    } else {
        status.customStatus
    }
    textView.text = newStatus
}

@BindingAdapter("statusImage")
fun bindStatusImage(appCompatImageButton: AppCompatImageButton, status: Status) {
    if (status.customStatus.isEmpty() && status.status.isNotEmpty()) {
        appCompatImageButton.visibility = View.INVISIBLE
    } else {
        appCompatImageButton.visibility = View.VISIBLE
    }
}