package com.naposystems.napoleonchat.ui.conversationCall

import android.graphics.Bitmap
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.utility.BlurTransformation
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Utils

@BindingAdapter("callBackground")
fun bindCallBackground(imageView: AppCompatImageView, @Nullable contact: ContactEntity?) {

    val context = imageView.context

    val defaultAvatar = ContextCompat.getDrawable(context, R.drawable.logo_napoleon_app_blur)

    if (contact != null) {

        val loadImage = when {
            contact.imageUrlFake.isNotEmpty() -> {
                contact.imageUrlFake
            }
            else -> {
                null
            }
        }

        val bitmapTransformation = mutableListOf<Transformation<Bitmap>>()
        if (loadImage != null) {
            bitmapTransformation.add(CenterCrop())
            bitmapTransformation.add(BlurTransformation(context))

        } else
            bitmapTransformation.add(CenterInside())

        Glide.with(context)
            .load(loadImage ?: defaultAvatar)
            .transform(*bitmapTransformation.toTypedArray())
            .into(imageView)
    } else {
        Glide.with(context)
            .load(defaultAvatar)
            .transform(CenterInside())
            .into(imageView)
    }

}

@BindingAdapter("callTitle")
fun bindCallTitle(textView: TextView, @Nullable contact: ContactEntity?) {
    if (contact != null) {
        val context = textView.context

        textView.text = context.getString(R.string.label_nickname, contact.getNickName())
    }
}

@BindingAdapter("callName")
fun bindCallName(textView: TextView, @Nullable contact: ContactEntity?) {
    if (contact != null) {
        val context = textView.context

        textView.text = context.getString(R.string.label_nickname, contact.getName())
    }
}

@BindingAdapter("callMessage")
fun bindCallMessage(textView: TextView, @Nullable contact: ContactEntity?) {
    if (contact != null) {
        val context = textView.context

        val formattedNickname = context.getString(R.string.label_nickname, contact.nicknameFake)

        val finalText = context.getString(R.string.text_contact_turn_off_camera, formattedNickname)

        textView.text = finalText
    }
}