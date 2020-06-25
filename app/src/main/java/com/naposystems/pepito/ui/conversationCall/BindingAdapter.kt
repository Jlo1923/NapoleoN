package com.naposystems.pepito.ui.conversationCall

import android.graphics.Bitmap
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.naposystems.pepito.R
import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.utility.BlurTransformation

@BindingAdapter("callBackground")
fun bindCallBackground(imageView: AppCompatImageView, @Nullable contact: Contact?) {

    val context = imageView.context

    val defaultAvatar = context.resources.getDrawable(
        R.drawable.logo_napoleon_app_blur,
        context.theme
    )

    val contactHasFoto = contact?.getImage()?.isNotEmpty() ?: false
    val bitmapTransformation = mutableListOf<Transformation<Bitmap>>()
    if (contactHasFoto) {
        bitmapTransformation.add(CenterCrop())
        bitmapTransformation.add(BlurTransformation(context))

    } else
        bitmapTransformation.add(CenterInside())

    Glide.with(context)
        .load(if (contactHasFoto) contact?.getImage() else defaultAvatar)
        .transform(*bitmapTransformation.toTypedArray())
        .into(imageView)

}

@BindingAdapter("callTitle")
fun bindCallTitle(textView: TextView, @Nullable contact: Contact?) {
    if (contact != null) {
        val context = textView.context

        textView.text = context.getString(R.string.label_nickname, contact.getNickName())
    }
}

@BindingAdapter("callName")
fun bindCallName(textView: TextView, @Nullable contact: Contact?) {
    if (contact != null) {
        val context = textView.context

        textView.text = context.getString(R.string.label_nickname, contact.getName())
    }
}

@BindingAdapter("callMessage")
fun bindCallMessage(textView: TextView, @Nullable contact: Contact?) {
    if (contact != null) {
        val context = textView.context

        val formattedNickname = when {
            contact.nicknameFake.isNotEmpty() -> {
                context.getString(R.string.label_nickname, contact.nicknameFake)
            }
            else -> {
                context.getString(R.string.label_nickname, contact.nickname)
            }
        }

        val finalText = context.getString(R.string.text_contact_turn_off_camera, formattedNickname)

        textView.text = finalText
    }
}