package com.naposystems.pepito.ui.profile.adapter

import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.naposystems.pepito.R
import com.naposystems.pepito.entity.User

@BindingAdapter("avatarUser")
fun bindAvatar(imageView: ImageView, @Nullable user: User?) {
    if (user != null) {
        val context = imageView.context

        val defaultAvatar = context.resources.getDrawable(
            R.drawable.ic_default_avatar,
            context.theme
        )

        @Suppress("IMPLICIT_CAST_TO_ANY")
        val loadImage = if (user.imageUrl.isNotEmpty()) {
            user.imageUrl
        } else {
            defaultAvatar
        }

        Glide.with(context)
            .load(loadImage)
            .circleCrop()
            .into(imageView)
    }
}

@BindingAdapter("nicknameUser")
fun bindNickname(textView: TextView, @Nullable user: User?) {
    if (user != null) {
        val context = textView.context
        val formattedNickname = context.getString(R.string.label_nickname, user.nickname)
        textView.text = formattedNickname
    }
}

@BindingAdapter("nameUser")
fun bindName(textView: TextView, @Nullable user: User?) {
    if (user != null) {
        textView.text = user.displayName
    }
}