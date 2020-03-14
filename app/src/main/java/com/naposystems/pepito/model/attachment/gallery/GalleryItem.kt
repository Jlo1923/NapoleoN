package com.naposystems.pepito.model.attachment.gallery

import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcelable
import androidx.recyclerview.widget.DiffUtil
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GalleryItem(
    val id: Int,
    val attachmentType: String = "",
    var thumbnailUri: Bitmap? = null,
    var contentUri: Uri? = null
) : Parcelable {
    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<GalleryItem>() {
            override fun areItemsTheSame(oldItem: GalleryItem, newItem: GalleryItem) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: GalleryItem, newItem: GalleryItem) =
                oldItem == newItem
        }
    }
}