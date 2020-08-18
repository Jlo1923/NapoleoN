package com.naposystems.napoleonchat.ui.blockedContacts.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.napoleonchat.databinding.BlockedContactsItemBinding
import com.naposystems.napoleonchat.entity.Contact

class BlockedContactsAdapter constructor(private val clickListener: BlockedContactsClickListener) :
    ListAdapter<Contact, BlockedContactsAdapter.BlockedContactsViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<Contact>() {
        override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem.nickname == newItem.nickname
        }

        override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BlockedContactsViewHolder {
        return BlockedContactsViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: BlockedContactsViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, clickListener)
    }

    class BlockedContactsViewHolder constructor(private val binding: BlockedContactsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Contact, clickListener: BlockedContactsClickListener) {
            binding.contact = item
            binding.clickListener = clickListener

            binding.buttonMore.setOnClickListener {
                clickListener.onMoreClick(item, it)
            }

            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): BlockedContactsViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = BlockedContactsItemBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
                return BlockedContactsViewHolder(binding)
            }
        }

    }

    interface BlockedContactsClickListener {
        fun onClick(item: Contact)
        fun onMoreClick(item: Contact, view: View)
    }
}