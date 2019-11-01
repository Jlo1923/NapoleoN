package com.naposystems.pepito.ui.languageSelection.adapter

import android.content.Context
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import com.naposystems.pepito.R

@BindingAdapter("flag")
fun bindFlag(imageView: AppCompatImageView, iso: String) {
    val context = imageView.context

    when (iso) {
        "de" -> imageView.setImageDrawable(
            getDrawable(
                R.drawable.ic_german_flag,
                context
            )
        )
        "en" -> imageView.setImageDrawable(
            getDrawable(
                R.drawable.ic_english_flag,
                context
            )
        )
        "es" -> imageView.setImageDrawable(
            getDrawable(
                R.drawable.ic_spanish_flag,
                context
            )
        )
        "fr" -> imageView.setImageDrawable(
            getDrawable(
                R.drawable.ic_france_flag,
                context
            )
        )
        "it" -> imageView.setImageDrawable(
            getDrawable(
                R.drawable.ic_italian_flag,
                context
            )
        )
        "pt" -> imageView.setImageDrawable(
            getDrawable(
                R.drawable.ic_brazil_flag,
                context
            )
        )
    }
}

private fun getDrawable(drawable: Int, context: Context) =
    context.resources.getDrawable(drawable, context.theme)