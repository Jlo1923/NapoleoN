package com.naposystems.napoleonchat.ui.previewMedia.adapter

import android.os.Handler
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.naposystems.napoleonchat.entity.message.MessageAndAttachment
import com.naposystems.napoleonchat.utility.Constants
import timber.log.Timber

@BindingAdapter("previewImage")
fun bindPreviewImage(imageView: ImageView, messageAndAttachment: MessageAndAttachment) {
    try {
        if (messageAndAttachment.attachmentList.isNotEmpty()) {
            imageView.visibility = View.VISIBLE
            val firstAttachment = messageAndAttachment.attachmentList[0]

            when (firstAttachment.type) {
                Constants.AttachmentType.IMAGE.type, Constants.AttachmentType.LOCATION.type -> {
                    Glide.with(imageView)
                        .load(firstAttachment)
                        .into(imageView)
                }
                Constants.AttachmentType.GIF.type -> {
                    Glide.with(imageView)
                        .asGif()
                        .load(firstAttachment)
                        .into(imageView)
                }
            }
        }
    } catch (e: Exception) {
        Timber.e(e)
    }
}

@BindingAdapter("setTextPreview")
fun bindSetTextPreview(textView: TextView, text: String = "") {
    textView.text = text

    Handler().postDelayed({
        val count = textView.lineCount
        if (count > 1) {
            textView.textAlignment = View.TEXT_ALIGNMENT_TEXT_START

            textView.setOnClickListener {
                if (textView.maxLines == 4) {
                    textView.maxLines = 20
                } else {
                    textView.maxLines = 4
                }
            }
        }
    }, 200)


}
