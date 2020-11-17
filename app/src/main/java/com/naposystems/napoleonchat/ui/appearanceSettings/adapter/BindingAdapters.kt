package com.naposystems.napoleonchat.ui.appearanceSettings.adapter

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.utility.Constants

@BindingAdapter("colorScheme")
fun bindColorScheme(textView: TextView, colorScheme: Int) {
    val context = textView.context

    val stringColorScheme = when (colorScheme) {
        Constants.ColorScheme.LIGHT_THEME.scheme -> context.getString(R.string.text_theme_light_napoleon)
        Constants.ColorScheme.DARK_THEME.scheme -> context.getString(R.string.text_theme_dark_napoleon)
        Constants.ColorScheme.BLACK_GOLD_ALLOY.scheme -> context.getString(R.string.text_theme_black_gold_alloy)
        Constants.ColorScheme.COLD_OCEAN.scheme -> context.getString(R.string.text_theme_cold_ocean)
        Constants.ColorScheme.CAMOUFLAGE.scheme -> context.getString(R.string.text_theme_camouflage)
        Constants.ColorScheme.PURPLE_BLUEBONNETS.scheme -> context.getString(R.string.text_theme_purple_bluebonnets)
        Constants.ColorScheme.PINK_DREAM.scheme -> context.getString(R.string.text_theme_pink_dream)
        else -> context.getString(R.string.text_theme_clear_sky)
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

@BindingAdapter("timeFormat")
fun bindTimeFormat(textView: TextView, timeFormat: Int) {
    val context = textView.context
    val stringTimeFormat = when (timeFormat) {
        Constants.TimeFormat.EVERY_TWENTY_FOUR_HOURS.time ->
            R.string.text_every_twenty_four_hours
        else -> R.string.text_every_twelve_hours
    }

    textView.text = context.getString(stringTimeFormat)
}