package com.naposystems.pepito.ui.custom

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.google.android.material.slider.Slider
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.CustomVerticalSliderBinding

class VerticalSlider constructor(context: Context, attributeSet: AttributeSet) :
        ConstraintLayout(context, attributeSet) {

    private val binding: CustomVerticalSliderBinding

    init {
        val infService = Context.LAYOUT_INFLATER_SERVICE
        val layoutInflater = getContext().getSystemService(infService) as LayoutInflater
        binding = DataBindingUtil.inflate(
                layoutInflater,
                R.layout.custom_vertical_slider,
                this@VerticalSlider,
                true
        )
    }
}