package com.naposystems.pepito.ui.blockedContacts.adapter

import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.naposystems.pepito.R
import com.naposystems.pepito.entity.BlockedContact

@BindingAdapter("avatarBlockedContact")
fun bindAvatar(imageView: ImageView, @Nullable blockedContact: BlockedContact?) {
    if (blockedContact != null) {
        val context = imageView.context

        val defaultAvatar = context.resources.getDrawable(
            R.drawable.ic_default_avatar,
            context.theme
        )

        @Suppress("IMPLICIT_CAST_TO_ANY")
        val loadImage = if (blockedContact.imageUrl.isNotEmpty()) {
            blockedContact.imageUrl
        } else {
            defaultAvatar
        }

        Glide.with(context)
            .load(loadImage)
            .circleCrop()
            .into(imageView)
    }
}

@BindingAdapter("nicknameBlockedContact")
fun bindNickname(textView: TextView, @Nullable blockedContact: BlockedContact?) {
    if (blockedContact != null) {
        val context = textView.context
        val formattedNickname = context.getString(R.string.label_nickname, blockedContact.nickname)
        textView.text = formattedNickname
    }
}

@BindingAdapter("nameBlockedContact")
fun bindName(textView: TextView, @Nullable blockedContact: BlockedContact?) {
    if (blockedContact != null) {
        textView.text = blockedContact.displayName
    }
}