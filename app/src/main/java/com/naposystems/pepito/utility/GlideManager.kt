package com.naposystems.pepito.utility

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.*
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import java.io.File

class GlideManager {

    companion object {

        fun loadFile(imageView: ImageView, file: File) {

            val target = object : CustomViewTarget<ImageView, Bitmap>(imageView) {
                override fun onLoadFailed(errorDrawable: Drawable?) {
                    imageView.setImageDrawable(errorDrawable)
                }

                override fun onResourceCleared(placeholder: Drawable?) {
                    imageView.setImageDrawable(placeholder)
                }

                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    imageView.setImageBitmap(resource)
                }
            }

            Glide.with(imageView)
                .asBitmap()
                .load(file)
                .into(target)
        }

        fun loadUri(imageView: ImageView, uri: Uri) {
            Glide.with(imageView)
                .load(uri)
                .into(imageView)
        }

        fun loadBitmap(imageView: ImageView, bitmap: Bitmap?) {
            if (bitmap != null) {
                Glide.with(imageView)
                    .asBitmap()
                    .load(bitmap)
                    .into(imageView)
            }
        }
    }
}