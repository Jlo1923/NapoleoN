package com.naposystems.napoleonchat.ui.profile.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.source.local.entity.UserEntity
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Utils

@BindingAdapter("avatarUser")
fun bindAvatar(imageView: ImageView, @Nullable userEntity: UserEntity?) {
    if (userEntity != null) {
        val context = imageView.context

        val defaultAvatar = context.resources.getDrawable(
            R.drawable.ic_default_avatar,
            context.theme
        )

        @Suppress("IMPLICIT_CAST_TO_ANY")
        val loadImage = if (userEntity.imageUrl.isNotEmpty()) {
            userEntity.imageUrl
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
fun bindBannerUser(imageView: ImageView, @Nullable userEntity: UserEntity?) {
    if (userEntity != null) {
        val context = imageView.context

        val defaultHeader = context.resources.getDrawable(
            R.drawable.bg_default_drawer_header,
            context.theme
        )

        @Suppress("IMPLICIT_CAST_TO_ANY")
        val loadImage = if (userEntity.headerUri.isNotEmpty()) {
             Utils.getFileUri(
                context = context,
                fileName = userEntity.headerUri,
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
fun bindNickname(textView: TextView, @Nullable userEntity: UserEntity?) {
    if (userEntity != null) {
        val context = textView.context
        val formattedNickname = context.getString(R.string.label_nickname, userEntity.nickname)
        textView.text = formattedNickname
    }
}

@BindingAdapter("nameUser")
fun bindName(textView: TextView, @Nullable userEntity: UserEntity?) {
    if (userEntity != null) {
        textView.text = userEntity.displayName
    }
}

@BindingAdapter("nameUserCover")
fun bindNameUserCover(textView: TextView, @Nullable userEntity: UserEntity?) {
    if (userEntity != null) {
        if (userEntity.displayName != ""){
            textView.visibility = View.VISIBLE
        } else {
            textView.visibility = View.GONE
        }

        textView.text = userEntity.displayName
    }
}