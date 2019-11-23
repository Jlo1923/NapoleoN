package com.naposystems.pepito.ui.home.adapter

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

@BindingAdapter("avatar")
fun bindAvatar(imageView: ImageView, imageUrl: String) {

    val context = imageView.context

    Glide
        .with(context)
        .load(imageUrl)
        .apply(RequestOptions().circleCrop())
        .into(imageView)
}