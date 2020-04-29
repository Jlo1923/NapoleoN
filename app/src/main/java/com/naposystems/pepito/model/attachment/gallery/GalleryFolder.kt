package com.naposystems.pepito.model.attachment.gallery

import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcelable
import androidx.recyclerview.widget.DiffUtil
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GalleryFolder(
    val id: Int,
    val folderName: String,
    val quantity: Int = 0,
    var thumbnailUri: Uri? = null,
    var bitmapThumbnail: Bitmap? = null,
    var contentUri: Uri? = null
) : Parcelable {
    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<GalleryFolder>() {
            override fun areItemsTheSame(oldItem: GalleryFolder, newItem: GalleryFolder) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: GalleryFolder, newItem: GalleryFolder) =
                oldItem == newItem
        }
    }
}