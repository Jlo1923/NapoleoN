package com.naposystems.napoleonchat.ui.previewMedia.adapter

import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.naposystems.napoleonchat.source.local.entity.MessageAttachmentRelation
import com.naposystems.napoleonchat.utility.Constants
import timber.log.Timber

@BindingAdapter("previewImage")
fun bindPreviewImage(imageView: ImageView, messageAndAttachmentRelation: MessageAttachmentRelation) {
    try {
        if (messageAndAttachmentRelation.attachmentEntityList.isNotEmpty()) {
            imageView.visibility = View.VISIBLE
            val firstAttachment = messageAndAttachmentRelation.attachmentEntityList[0]

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

    Handler(Looper.getMainLooper()).postDelayed({
        if (text.isEmpty()) textView.isVisible = false

        val count = textView.lineCount
        if (count > 3) {
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
