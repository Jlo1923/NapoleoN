package com.naposystems.napoleonchat.ui.napoleonKeyboardStickerPage.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.StorageReference
import com.naposystems.napoleonchat.databinding.NapoleonKeyboardStickerViewpagerItemBinding
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.FileManager
import java.io.File

class NapoleonKeyboardStickerPageAdapter constructor(private val listener: NapoleonKeyboardStickerPageListener) :
    ListAdapter<StorageReference, NapoleonKeyboardStickerPageAdapter.NapoleonGifViewHolder>(
        DiffCallback
    ) {

    object DiffCallback : DiffUtil.ItemCallback<StorageReference>() {
        override fun areItemsTheSame(
            oldItem: StorageReference,
            newItem: StorageReference
        ): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(
            oldItem: StorageReference,
            newItem: StorageReference
        ): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NapoleonGifViewHolder {
        return NapoleonGifViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: NapoleonGifViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, listener)
    }

    class NapoleonGifViewHolder constructor(private val binding: NapoleonKeyboardStickerViewpagerItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: StorageReference, clickListener: NapoleonKeyboardStickerPageListener) {
            binding.clickListener = clickListener

            val file = FileManager.createFile(
                binding.imageViewProgress.context,
                item.name,
                Constants.CacheDirectories.GIFS.folder
            )

            binding.file = file

            if (!file.exists()) {
                item.getFile(file)
                    .addOnSuccessListener {
                        binding.imageViewProgress.loadImageFileAsGif(file)
                    }
            } else {
                binding.imageViewProgress.loadImageFileAsGif(file)
            }

            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): NapoleonGifViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = NapoleonKeyboardStickerViewpagerItemBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
                return NapoleonGifViewHolder(binding)
            }
        }

    }

    interface NapoleonKeyboardStickerPageListener {
        fun onGifSelected(file: File)
    }

}