package com.naposystems.napoleonchat.ui.attachmentGalleryFolder.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.napoleonchat.databinding.AttachmentGalleryFoldersItemBinding
import com.naposystems.napoleonchat.model.attachment.gallery.GalleryFolder

class AttachmentGalleryFolderAdapter constructor(private val clickListener: ClickListener) :
    ListAdapter<GalleryFolder, AttachmentGalleryFolderAdapter.AttachmentGalleryFolderViewHolder>(GalleryFolder.DiffCallback) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AttachmentGalleryFolderViewHolder {
        return AttachmentGalleryFolderViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: AttachmentGalleryFolderViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, clickListener)
    }

    class AttachmentGalleryFolderViewHolder constructor(private val binding: AttachmentGalleryFoldersItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: GalleryFolder, clickListener: ClickListener) {
            binding.galleryFolder = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): AttachmentGalleryFolderViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = AttachmentGalleryFoldersItemBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )

                return AttachmentGalleryFolderViewHolder(binding)
            }
        }

    }

    interface ClickListener {
        fun onClick(galleryFolder: GalleryFolder)
    }
}