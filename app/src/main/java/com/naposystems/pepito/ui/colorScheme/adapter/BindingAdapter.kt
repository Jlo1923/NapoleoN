package com.naposystems.pepito.ui.colorScheme.adapter

import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter

@BindingAdapter("showIcon")
fun bindShowIcon(imageView: ImageView, isVisible: Boolean) {
    imageView.visibility = if (isVisible) View.VISIBLE else View.GONE
}