package com.naposystems.napoleonchat.ui.status.adapter

import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.databinding.BindingAdapter
import com.naposystems.napoleonchat.source.local.entity.StatusEntity

@BindingAdapter("status")
fun bindStatus(textView: TextView, statusEntity: StatusEntity) {
    val newStatus = if (statusEntity.customStatus.isEmpty() && statusEntity.status.isNotEmpty()) {
        statusEntity.status
    } else {
        statusEntity.customStatus
    }
    textView.text = newStatus
}

@BindingAdapter("statusImage")
fun bindStatusImage(appCompatImageButton: AppCompatImageButton, statusEntity: StatusEntity) {
    if (statusEntity.customStatus.isEmpty() && statusEntity.status.isNotEmpty()) {
        appCompatImageButton.visibility = View.INVISIBLE
    } else {
        appCompatImageButton.visibility = View.VISIBLE
    }
}