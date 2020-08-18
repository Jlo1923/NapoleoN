package com.naposystems.napoleonchat.ui.attachmentGallery.adapter

import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.napoleonchat.databinding.AttachmentGalleryItemBinding
import com.naposystems.napoleonchat.model.attachment.gallery.GalleryItem
import com.naposystems.napoleonchat.utility.BaseCursorAdapter
import com.naposystems.napoleonchat.utility.Constants
import timber.log.Timber

class AttachmentGalleryAdapter constructor(private val clickListener: ClickListener) :
    BaseCursorAdapter<AttachmentGalleryAdapter.AttachmentGalleryViewHolder>(null) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AttachmentGalleryViewHolder {
        return AttachmentGalleryViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: AttachmentGalleryViewHolder, cursor: Cursor?) {
        cursor?.let { newCursor ->
            val idColumnIndex =
                newCursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val mediaTypeColumnIndex =
                newCursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
            val sizeColumnIndex = newCursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)

            val fileId = newCursor.getInt(idColumnIndex)
            val mediaType = newCursor.getInt(mediaTypeColumnIndex)
            val size = newCursor.getString(sizeColumnIndex)

            val attachmentType = when (mediaType) {
                MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE -> Constants.AttachmentType.IMAGE.type
                MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO -> Constants.AttachmentType.VIDEO.type
                else -> ""
            }

            Timber.d("Size gallery: $size, mediaType: $attachmentType")

            val galleryItem = GalleryItem(
                id = fileId,
                attachmentType = attachmentType
            )

            var externalUri: Uri? = null

            if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
                externalUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            } else if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
                externalUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }

            val contentUri = ContentUris.withAppendedId(
                externalUri!!,
                fileId.toLong()
            )

            galleryItem.contentUri = contentUri

            holder.bind(galleryItem, clickListener)
        }
    }

    /*override fun onBindViewHolder(holder: AttachmentGalleryViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item, clickListener)
        }
    }*/

    class AttachmentGalleryViewHolder constructor(private val binding: AttachmentGalleryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: GalleryItem, clickListener: ClickListener) {
            binding.container.setOnClickListener {
                clickListener.onClick(
                    galleryItem = item,
                    imageView = binding.imageViewFolderThumbnail
                )
            }

            binding.galleryItem = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): AttachmentGalleryViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = AttachmentGalleryItemBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )

                return AttachmentGalleryViewHolder(binding)
            }
        }

    }

    interface ClickListener {
        fun onClick(galleryItem: GalleryItem, imageView: ImageView)
    }
}