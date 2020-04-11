package com.naposystems.pepito.ui.colorScheme.adapter

import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.naposystems.pepito.R
import com.naposystems.pepito.utility.Constants

@BindingAdapter("showIcon")
fun bindShowIcon(imageView: ImageView, isVisible: Boolean) {
    imageView.visibility = if (isVisible) View.VISIBLE else View.GONE
}

@BindingAdapter("previewTheme")
fun bindPreviewTheme(imageView: ImageView, theme: Int) {
    when(theme) {
        Constants.ThemesApplication.LIGHT_NAPOLEON.theme -> applyDrawable(imageView, R.drawable.theme_light_napoleon)
        Constants.ThemesApplication.DARK_NAPOLEON.theme -> applyDrawable(imageView, R.drawable.theme_dark_napoleon)
        Constants.ThemesApplication.BLACK_GOLD_ALLOY.theme -> applyDrawable(imageView, R.drawable.theme_black_gold_alloy)
        Constants.ThemesApplication.COLD_OCEAN.theme -> applyDrawable(imageView, R.drawable.theme_cold_ocean)
        Constants.ThemesApplication.CAMOUFLAGE.theme -> applyDrawable(imageView, R.drawable.theme_camouflage)
        Constants.ThemesApplication.PURPLE_BLUEBONNETS.theme -> applyDrawable(imageView, R.drawable.theme_purple_bluebonnets)
        Constants.ThemesApplication.PINK_DREAM.theme -> applyDrawable(imageView, R.drawable.theme_pink_dream)
        Constants.ThemesApplication.CLEAR_SKY.theme -> applyDrawable(imageView, R.drawable.theme_clear_sky)
    }
}

private fun applyDrawable(imageView: ImageView, theme: Int) {
    imageView.context.let { context ->
        imageView.setImageDrawable(
            context.resources.getDrawable(theme, context.theme)
        )
    }
}