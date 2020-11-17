package com.naposystems.napoleonchat.ui.attachmentAudio.adapter

import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.entity.message.attachments.MediaStoreAudio
import com.naposystems.napoleonchat.utility.Utils
import javax.annotation.Nullable


@BindingAdapter("durationAndSize")
fun bindDuration(textView: TextView, mediaStoreAudio: MediaStoreAudio) {
    val duration = Utils.getDuration(mediaStoreAudio.duration, false)
    val size = Utils.getFileSize(mediaStoreAudio.size)
    textView.text = textView.context.getString(R.string.text_duration_and_size, duration, size)
}

@BindingAdapter("fileSize")
fun binFileSize(textView: TextView, size: Long) {
    textView.text = Utils.getFileSize(size)
}

@BindingAdapter("isMediaSelected")
fun bindIsSelected(imageView: AppCompatImageView, isSelected: Boolean) {
    if (isSelected) {
        val animation: Animation = AnimationUtils.loadAnimation(imageView.context, R.anim.scale_up)
        imageView.visibility = View.VISIBLE
        imageView.startAnimation(animation)
    } else {
        imageView.visibility = View.GONE
    }
}

@BindingAdapter("albumArt")
fun bindAlbumArt(imageView: AppCompatImageView, @Nullable albumArt: String?) {
    if (albumArt != null) {
        val bm = BitmapFactory.decodeFile(albumArt)

        Glide.with(imageView)
            .load(bm)
            .addListener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    imageView.setImageResource(R.drawable.ic_audio)
                    return true
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    imageView.setImageDrawable(resource)
                    return true
                }
            })
            .into(imageView)
    } else {
        imageView.setImageResource(R.drawable.ic_audio)
    }
}