package com.naposystems.napoleonchat.ui.attachmentLocation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.napoleonchat.databinding.AttachmentLocationItemBinding
import com.naposystems.napoleonchat.model.attachment.location.Place

class AttachmentLocationAdapter constructor(private val listener: AttachmentLocationListener) :
    ListAdapter<Place, AttachmentLocationAdapter.AttachmentLocationViewHolder>(Place.DiffCallback) {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = AttachmentLocationViewHolder.from(parent)

    override fun onBindViewHolder(holder: AttachmentLocationViewHolder, position: Int) {
        val place = getItem(position)
        holder.bind(place, listener)
    }

    class AttachmentLocationViewHolder constructor(private val binding: AttachmentLocationItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Place, clickListener: AttachmentLocationListener) {
            binding.place = item
            binding.clickListener = clickListener

            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): AttachmentLocationViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = AttachmentLocationItemBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
                return AttachmentLocationViewHolder(binding)
            }
        }
    }

    interface AttachmentLocationListener {
        fun onPlaceSelected(place: Place)
    }

}