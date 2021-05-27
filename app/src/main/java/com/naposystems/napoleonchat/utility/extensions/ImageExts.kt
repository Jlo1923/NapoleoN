package com.naposystems.napoleonchat.utility.extensions

import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.naposystems.napoleonchat.utility.BlurTransformation

fun getBlurTransformation(
    context: Context
): Array<Transformation<Bitmap>> {
    val transformationList: MutableList<Transformation<Bitmap>> = arrayListOf()
    transformationList.apply {
        add(CenterCrop())
        add(BlurTransformation(context))
        add(RoundedCorners(8))
    }
    return transformationList.toTypedArray()
}