package com.naposystems.pepito.ui.status.adapter

import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.databinding.BindingAdapter
import com.naposystems.pepito.entity.Status

@BindingAdapter("status")
fun bindStatus(textView: TextView, status: Status) {
    val context = textView.context
    val newStatus = if (status.customStatus.isEmpty() && status.resourceId > 0) {
        context.getString(status.resourceId)
    } else {
        status.customStatus
    }
    textView.text = newStatus
}

@BindingAdapter("statusImage")
fun bindStatusImage(appCompatImageButton: AppCompatImageButton, status: Status) {
    if (status.customStatus.isEmpty() && status.resourceId > 0) {
        appCompatImageButton.visibility = View.INVISIBLE
    } else {
        appCompatImageButton.visibility = View.VISIBLE
    }
}