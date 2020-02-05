package com.naposystems.pepito.ui.contactProfile.adapter

import android.annotation.SuppressLint
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.naposystems.pepito.R
import com.naposystems.pepito.entity.Contact

@BindingAdapter("nameContact")
fun bindNameContact(textView: TextView, contact: Contact?) {
    if(contact != null) {
        textView.text = contact.displayName
    }
}

@SuppressLint("SetTextI18n")
@BindingAdapter("nicknameContact")
fun bindNickNameContactWithAt(textView: TextView, contact: Contact?) {
    if(contact != null) {
        textView.text = "@"+contact.nickname
    }
}

@SuppressLint("SetTextI18n")
@BindingAdapter("nicknameContact")
fun bindNickNameContact(textView: TextView, contact: Contact?) {
    if(contact != null) {
        textView.text = "@"+contact.nickname
    }
}

@BindingAdapter("imageContact")
fun bindImageContact(imageView: ImageView, @Nullable contact: Contact?) {

    if (contact != null) {
        val context = imageView.context

        val defaultAvatar = context.resources.getDrawable(
            R.drawable.ic_default_avatar,
            context.theme
        )

        Glide.with(context)
            .load(
                if (contact.imageUrl.isEmpty()) defaultAvatar else contact.imageUrl
            )
            //.circleCrop()
            .into(imageView)
    }
}