package com.naposystems.pepito.utility.adapters

import android.net.Uri
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.naposystems.pepito.R

@BindingAdapter("background")
fun bindBackground(imageView: ImageView, imageUrl: String) {
    val context = imageView.context

    val defaultBackground = context.resources.getDrawable(
        R.drawable.bg_default_drawer_header,
        context.theme
    )

    Glide.with(context)
        .load(
            if (imageUrl.isEmpty()) defaultBackground else Uri.parse(imageUrl)
        )
        .into(imageView)
}

@BindingAdapter("avatar")
fun bindAvatar(imageView: ImageView, imageUrl: String) {

    val context = imageView.context
    val defaultAvatar = context.resources.getDrawable(
        R.drawable.ic_default_avatar,
        context.theme
    )

    Glide.with(context)
        .load(
            if (imageUrl.isEmpty()) defaultAvatar else imageUrl
        )
        .circleCrop()
        .into(imageView)
}

@BindingAdapter("nickname")
fun bindNickname(textView: TextView, nickname: String) {
    val context = textView.context
    val formattedNickname = context.getString(R.string.label_nickname, nickname)

    textView.text = formattedNickname
}