package com.naposystems.napoleonchat.ui.conversationCall

import android.graphics.Bitmap
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.entity.Contact
import com.naposystems.napoleonchat.utility.BlurTransformation
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Utils

@BindingAdapter("callBackground")
fun bindCallBackground(imageView: AppCompatImageView, @Nullable contact: Contact?) {

    val context = imageView.context

    val defaultAvatar = context.resources.getDrawable(
        R.drawable.logo_napoleon_app_blur,
        context.theme
    )

    if (contact != null) {

        val loadImage = when {
            contact.imageUrlFake.isNotEmpty() -> {
                Utils.getFileUri(
                    context = context,
                    fileName = contact.imageUrlFake,
                    subFolder = Constants.NapoleonCacheDirectories.IMAGE_FAKE_CONTACT.folder
                )
            }
            contact.imageUrl.isNotEmpty() -> {
                contact.imageUrl
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