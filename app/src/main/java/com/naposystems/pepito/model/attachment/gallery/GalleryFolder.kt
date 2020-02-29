package com.naposystems.pepito.model.attachment.gallery

import android.net.Uri
import androidx.recyclerview.widget.DiffUtil

data class GalleryFolder(
    val id: Int,
    val folderName: String,
    val quantity: Int = 0,
    var thumbnailUri: Uri? = null,
    var contentUri: Uri? = null
) {
    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<GalleryFolder>() {
            override fun areItemsTheSame(oldItem: GalleryFolder, newItem: GalleryFolder) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: GalleryFolder, newItem: GalleryFolder) =
                oldItem == newItem
        }
    }
}