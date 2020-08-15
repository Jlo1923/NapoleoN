package com.naposystems.napoleonchat.ui.profile.adapter

import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.entity.User
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Utils

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

@BindingAdapter("bannerUser")
fun bindBannerUser(imageView: ImageView, @Nullable user: User?) {
    if (user != null) {
        val context = imageView.context

        val defaultHeader = context.resources.getDrawable(
            R.drawable.bg_default_drawer_header,
            context.theme
        )

        @Suppress("IMPLICIT_CAST_TO_ANY")
        val loadImage = if (user.headerUri.isNotEmpty()) {
             Utils.getFileUri(
                context = context,
                fileName = user.headerUri,
                subFolder = Constants.NapoleonCacheDirectories.HEADER.folder
            )
        } else {
            defaultHeader
        }

        Glide.with(imageView)
            .load(loadImage)
            .centerCrop()
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