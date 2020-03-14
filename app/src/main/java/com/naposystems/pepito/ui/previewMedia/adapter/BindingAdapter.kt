package com.naposystems.pepito.ui.previewMedia.adapter

import android.content.ContentUris
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.GlideManager

@BindingAdapter("conversationPreviewImage")
fun bindConversationPreviewImage(imageView: ImageView, messageAndAttachment: MessageAndAttachment) {

    val context = imageView.context

    if (messageAndAttachment.attachmentList.isNotEmpty()) {
        val firstAttachment = messageAndAttachment.attachmentList[0]

        if (firstAttachment.type == Constants.AttachmentType.IMAGE.type) {
            val contentUri = ContentUris.withAppendedId(
                MediaStore.Files.getContentUri("external"),
                ContentUris.parseId(Uri.parse(firstAttachment.uri))
            )

            var bitmap: Bitmap
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ImageDecoder.createSource(context.contentResolver, contentUri)
                    .also { source ->
                        ImageDecoder.decodeBitmap(source).also { bitmapDecoded ->
                            bitmap = bitmapDecoded
                        }
                    }
            } else {
                bitmap =
                    MediaStore.Images.Media.getBitmap(
                        context.contentResolver,
                        contentUri
                    )
            }

            GlideManager.loadBitmap(
                imageView,
                bitmap
            )
        }
    }
}