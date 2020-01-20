package com.naposystems.pepito.ui.appearanceSettings.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.naposystems.pepito.R
import com.naposystems.pepito.utility.Constants

@BindingAdapter("colorScheme")
fun bindColorScheme(textView: TextView, colorScheme: Int) {
    val context = textView.context

    val stringColorScheme = when (colorScheme) {
        Constants.ColorScheme.LIGHT_THEME.scheme -> context.getString(R.string.text_light_theme)
        Constants.ColorScheme.DARK_THEME.scheme -> context.getString(R.string.text_dark_theme)
        else -> context.getString(R.string.text_light_theme)
    }

    textView.text = stringColorScheme
}

@BindingAdapter("userDisplayFormat")
fun bindUserDisplayFormat(textView: TextView, userDisplayFormat: Int) {
    val context = textView.context

    val stringUserDisplayFormat = when (userDisplayFormat) {
        Constants.UserDisplayFormat.NAME_AND_NICKNAME.format ->
            context.getString(R.string.text_name_and_nickname)
        Constants.UserDisplayFormat.ONLY_NAME.format ->
            context.getString(R.string.text_only_name)
        Constants.UserDisplayFormat.ONLY_NICKNAME.format ->
            context.getString(R.string.text_only_nickname)
        else -> context.getString(R.string.text_name_and_nickname)
    }

    textView.text = stringUserDisplayFormat
}