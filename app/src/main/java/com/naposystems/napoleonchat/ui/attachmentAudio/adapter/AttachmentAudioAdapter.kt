package com.naposystems.napoleonchat.ui.attachmentAudio.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.napoleonchat.databinding.AttachmentAudioItemBinding
import com.naposystems.napoleonchat.entity.message.attachments.MediaStoreAudio
import com.naposystems.napoleonchat.ui.custom.animatedTwoVectorView.AnimatedTwoVectorView

class AttachmentAudioAdapter constructor(private val clickListener: ClickListener) :
    ListAdapter<MediaStoreAudio, AttachmentAudioAdapter.AttachmentAudioViewHolder>(MediaStoreAudio.DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachmentAudioViewHolder {
        return AttachmentAudioViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: AttachmentAudioViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, clickListener)
    }

    class AttachmentAudioViewHolder constructor(private val binding: AttachmentAudioItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MediaStoreAudio, clickListener: ClickListener) {
            binding.mediaStoreAudio = item
            binding.clickListener = clickListener

            binding.imageButtonPlay.setOnClickListener {
                clickListener.onPlayClick(item, binding.imageButtonPlay)
            }
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): AttachmentAudioViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = AttachmentAudioItemBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )

                return AttachmentAudioViewHolder(binding)
            }
        }

    }

    interface ClickListener {
        fun onClick(mediaStoreAudio: MediaStoreAudio)
        fun onPlayClick(
            mediaStoreAudio: MediaStoreAudio,
            imageButtonPlay: AnimatedTwoVectorView
        )
    }
}