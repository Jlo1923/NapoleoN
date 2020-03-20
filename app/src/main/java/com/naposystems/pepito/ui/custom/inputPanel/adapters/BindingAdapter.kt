package com.naposystems.pepito.ui.custom.inputPanel.adapters

import android.net.Uri
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.naposystems.pepito.R

@BindingAdapter("imageQuote")
fun bindImageQuote(imageView: ImageView, imageUrl: String) {
    val context = imageView.context

    val defaultBackground = context.resources.getDrawable(
        R.drawable.vertical_photo,
        context.theme
    )

    Glide.with(context)
        .load(
            if (imageUrl.isEmpty()) defaultBackground else Uri.parse(imageUrl)
        )
        .transform(CenterCrop(),RoundedCorners(8))
        .into(imageView)
}