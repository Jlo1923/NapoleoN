package com.naposystems.pepito.ui.attachmentGallery.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.pepito.databinding.AttachmentGalleryItemBinding
import com.naposystems.pepito.model.attachment.gallery.GalleryItem
import com.naposystems.pepito.ui.attachmentGallery.AttachmentGalleryFragmentDirections

class AttachmentGalleryAdapter constructor(private val clickListener: ClickListener) :
    PagedListAdapter<GalleryItem, AttachmentGalleryAdapter.AttachmentGalleryViewHolder>(GalleryItem.DiffCallback) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AttachmentGalleryViewHolder {
        return AttachmentGalleryViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: AttachmentGalleryViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item, clickListener)
        }
    }

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